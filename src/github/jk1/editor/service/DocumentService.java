package github.jk1.editor.service;

import github.jk1.editor.Diff;
import github.jk1.editor.Document;
import github.jk1.editor.MobWriteMessage;
import github.jk1.editor.View;
import name.fraser.neil.plaintext.StandardBreakScorer;
import name.fraser.neil.plaintext.diff_match_patch;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.logging.Logger;

/**
 * @author Evgeny Naumenko
 */
@Service
public class DocumentService {

    private static final Logger LOGGER = Logger.getLogger(DocumentService.class.getName());
    private diff_match_patch diffMatchPatch = new diff_match_patch(new StandardBreakScorer());

    /**
     * @param view
     * @param message
     */
    public void process(View view, MobWriteMessage message) {
        LOGGER.info(message.toString());
        if (message.getServerVersion() != view.getShadowServerVersion()
                && message.getServerVersion() == view.getBackUpShadowServerVersion()) {
            //client missed the last update, rollback the shadow
            view.rollback();
        }
        view.dropObsoleteEditStackRecords(message.getServerVersion());
        for (Diff diff : message.getDiffs()) {
            if (diff.getMode() == Diff.Mode.DELTA) {
                processDelta(view, message, diff);
            } else {
                processRaw(view, message, diff);
            }
        }
    }

    private void processRaw(View view, MobWriteMessage mobWriteMessage, Diff diff) {
        view.setShadow(diff.getPayload());
        view.setShadowClientVersion(diff.getVersion());
        view.setShadowServerVersion(mobWriteMessage.getServerVersion());
        view.setBackupShadow(view.getShadow());
        view.setBackUpShadowServerVersion(view.getShadowServerVersion());
        view.getEditStack().clear();
    }

    private void processDelta(View view, MobWriteMessage message, Diff diff) {
        if (this.assertDelta(view, message, diff)) {
            LinkedList<diff_match_patch.Diff> diffs = diffMatchPatch.diff_fromDelta(view.getShadow(), diff.getPayload());
            view.incrementShadowClientVersion();
            synchronized (view.getDocument()) {
                LinkedList<diff_match_patch.Patch> patches = diffMatchPatch.patch_make(view.getShadow(), diffs);
                view.setShadow(diffMatchPatch.diff_text2(diffs));
                view.setBackupShadow(view.getShadow());
                view.setBackUpShadowServerVersion(view.getShadowServerVersion());
                Document doc = view.getDocument();
                doc.setText(diffMatchPatch.patch_apply(patches, doc.getText())[0].toString());
            }
        }
    }

    private boolean assertDelta(View view, MobWriteMessage message, Diff diff) {
        if (view.getShadowServerVersion() != message.getServerVersion()) {
            view.invalidateDelta();
            String template = "Server version mismatch detected, server %d, client %d. Dropping client diff";
            LOGGER.warning(String.format(template, view.getShadowServerVersion(), message.getServerVersion()));
            return false;
        } else if (diff.getVersion() > view.getShadowClientVersion()) {
            view.invalidateDelta();
            String template = "Client version is from the future, server %d, client %d. Dropping client diff";
            LOGGER.warning(String.format(template, view.getShadowClientVersion(), diff.getVersion()));
            return false;
        } else if (diff.getVersion() > view.getShadowClientVersion()) {
            //deja vu, server has already received this diff
            String template = "Client version is from the past, server %d, client %d. Dropping client diff";
            LOGGER.info(String.format(template, view.getShadowClientVersion(), diff.getVersion()));
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
     * @param view            server-side view as delta computation base
     * @param mobWriteMessage client request to prepare a response for
     * @return raw mobwrite protocol message for the client
     * @see <a href="https://code.google.com/p/google-mobwrite/wiki/Protocol">MobWrite protocol reference</a>
     */
    public String generateDiffs(View view, MobWriteMessage mobWriteMessage) {
        String masterText = view.getDocument().getText();
        Diff diff;
        if (view.isDeltaOk()) {
            LinkedList<diff_match_patch.Diff> diffs = diffMatchPatch.diff_main(view.getShadow(), masterText);
            String diffString = diffMatchPatch.diff_toDelta(diffs);
            diff = new Diff(Diff.Mode.DELTA, view.getShadowServerVersion(), diffString);
        } else {
            // server could not parse client's delta, sent raw response back
            diff = new Diff(Diff.Mode.RAW, view.getShadowServerVersion(), masterText);
        }
        view.getEditStack().add(diff);
        view.incrementShadowServerVersion();
        view.setShadow(masterText);
        StringBuilder builder = new StringBuilder();
        builder.append("f:").append(view.getShadowClientVersion())
                .append(':').append(mobWriteMessage.getDocumentName()).append('\n');
        for (Diff element : view.getEditStack()) {
            builder.append(element);
        }
        return builder.append('\n').toString();
    }
}
