package github.jk1.editor.model;

import name.fraser.neil.plaintext.diff_match_patch;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Logger;

/**
 * Represents a document view for a particular client editor instance.
 * A single user may open several editors in scope of the one web session,
 * and all his editors are treated independently, as though the have been
 * created by different users.
 * <p/>
 * View uses a "shadow" as a representation of what server thinks client text is.
 * "Backup shadow" is effectively a "shadow" one version back, to roll the shadow back
 * in case of a temporary network failure.
 * <p/>
 * View is thread-safe, although serves requests from a single client.
 *
 * @author Evgeny Naumenko
 */
public class View {

    private static final Logger LOGGER = Logger.getLogger(View.class.getName());

    private final diff_match_patch diffMatchPatch;
    private final Document document;
    private final String token;
    private String shadow;
    private String backupShadow;
    private int shadowClientVersion = 0;
    private int shadowServerVersion = 0;
    private int backUpShadowServerVersion = 0;
    /**
     * State flag for any kind of communication errors: parsing problems, delta version mismatch, etc.
     */
    private boolean deltaOk = true;
    /**
     * Owner's cursor position at the moment of the last update. Volatile as getter is not synchronized
     */
    private volatile int cursorPosition = 0;

    /**
     * Pending diffs to be sent to the client. We can accumulate diffs here
     * and recover from the network failure, if any
     */
    private LinkedList<Diff> editStack = new LinkedList<>();

    public View(diff_match_patch diffMatchPatch, Document document, String token) {
        this.diffMatchPatch = diffMatchPatch;
        this.document = document;
        this.token = token;
        this.shadow = document.getText();
        this.shadow = document.getTitle();
    }

    /**
     * Applies client diff message to the current view and generates response
     * diff for the client.
     */
    public synchronized MobWriteResponse apply(MobWriteRequest clientMessage) {
        LOGGER.info(clientMessage.toString());
        deltaOk = true;
        if (clientMessage.getVersion() != shadowServerVersion
                && clientMessage.getVersion() == backUpShadowServerVersion) {
            //client missed the last update, rollback the shadow
            this.rollback();
        }
        this.dropObsoleteEditStackRecords(clientMessage.getVersion());
        for (Diff diff : clientMessage.getDiffs()) {
            if (diff.getMode() == Diff.Mode.DELTA) {
                this.processDelta(clientMessage, diff);
            } else {
                this.processRaw(clientMessage, diff);
                break; // later deltas make little sense on the server copy
            }
        }
        this.cursorPosition = clientMessage.getCursorPosition();
        return this.generateResponse(clientMessage);
    }

    private void rollback() {
        LOGGER.info("Rolling back view " + token + " of document " + document.getId());
        this.shadow = backupShadow;
        this.shadowServerVersion = backUpShadowServerVersion;
        editStack.clear();
    }

    private void dropObsoleteEditStackRecords(int currentVersion) {
        Iterator<Diff> iterator = editStack.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getVersion() < currentVersion) {
                iterator.remove();
            }
        }
    }

    /**
     * Handles client request for entire raw document state
     */
    private void processRaw(MobWriteMessage clientMessage, Diff diff) {
        shadow = diff.getPayload();
        shadowClientVersion = diff.getVersion();
        shadowServerVersion = clientMessage.getVersion();
        backupShadow = shadow;
        backUpShadowServerVersion = shadowServerVersion;
        editStack.clear();
    }

    /**
     * Handles client's post of incremental delta
     */
    private void processDelta(MobWriteRequest clientMessage, Diff diff) {
        try {
            if (this.assertDelta(clientMessage, diff)) {
                LinkedList<diff_match_patch.Diff> diffs = diffMatchPatch.diff_fromDelta(shadow, diff.getPayload());
                shadowClientVersion++;
                synchronized (document) {
                    LinkedList<diff_match_patch.Patch> patches = diffMatchPatch.patch_make(shadow, diffs);
                    shadow = diffMatchPatch.diff_text2(diffs);
                    backupShadow = shadow;
                    backUpShadowServerVersion = shadowServerVersion;
                    String newText = diffMatchPatch.patch_apply(patches, document.getText())[0].toString();
                    if (!newText.equals(document.getText())) {
                        clientMessage.setDeltaEmpty(false);
                        document.setText(newText);
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            deltaOk = false;
            LOGGER.warning("Delta application failure: " + e.getMessage());
        }
    }

    private boolean assertDelta(MobWriteMessage message, Diff diff) {
        if (shadowServerVersion != message.getVersion()) {
            deltaOk = false;
            String template = "Server version mismatch detected, server %d, client %d. Dropping client diff";
            LOGGER.warning(String.format(template, shadowServerVersion, message.getVersion()));
            return false;
        } else if (diff.getVersion() > shadowClientVersion) {
            deltaOk = false;
            String template = "Client version is from the future, server %d, client %d. Dropping client diff";
            LOGGER.warning(String.format(template, shadowClientVersion, diff.getVersion()));
            return false;
        } else if (diff.getVersion() < shadowClientVersion) {
            //deja vu, server has already received this diff
            String template = "Client version is from the past, server %d, client %d. Dropping client diff";
            LOGGER.info(String.format(template, shadowClientVersion, diff.getVersion()));
            return false;
        } else {
            return true;
        }
    }

    /**
     * Creates a diff set for a client to apply based on current server contents.
     * If client and server are out of sync, then server will send a raw text data
     * to the client as a new shared text base, effectively voiding client changes.
     * Sad, but true.
     *
     * @param req client request to prepare a response for
     * @return raw mobwrite protocol clientMessage for the client
     * @see <a href="https://code.google.com/p/google-mobwrite/wiki/Protocol">MobWrite protocol reference</a>
     */
    private MobWriteResponse generateResponse(MobWriteMessage req) {
        String masterText = document.getText();
        Diff diff;
        if (deltaOk) {
            LinkedList<diff_match_patch.Diff> diffs = diffMatchPatch.diff_main(shadow, masterText);
            String diffString = diffMatchPatch.diff_toDelta(diffs);
            diff = new Diff(Diff.Mode.DELTA, shadowServerVersion, diffString);
        } else {
            // server could not parse client's delta, sent raw response back
            diff = new Diff(Diff.Mode.RAW, shadowServerVersion, masterText);
        }
        editStack.add(diff);
        shadowServerVersion++;
        shadow = masterText;
        MobWriteResponse message = new MobWriteResponse(req.getDocumentName(), req.getToken(), shadowClientVersion);
        for (Diff element : editStack) {
            message.addDiff(element);
        }
        message.setResponseCursors(document.getConcurrentCursors(req.getToken()));
        return message;
    }

    /**
     * @return owner's cursor position from last sync
     */
    public int getCursorPosition() {
        return cursorPosition;
    }
}
