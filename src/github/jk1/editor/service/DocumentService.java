package github.jk1.editor.service;

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

    private static final Logger logger = Logger.getLogger(DocumentService.class.getName());
    private diff_match_patch diffMatchPatch = new diff_match_patch(new StandardBreakScorer());

    /**
     * @param view
     * @param message
     */
    public void process(View view, MobWriteMessage message) {
        logger.info(message.toString());
        view.setDeltaOk(true);
        if (message.getServerVersion() != view.getShadowServerVersion()
                || message.getServerVersion() == view.getBackUpShadowServerVersion()) {
            //client missed the last update, rollback the shadow
            view.rollback();
        }
        view.dropObsoleteEditStackRecords(message.getServerVersion());
        if (message.getMode() == MobWriteMessage.Mode.DELTA) {
            processDelta(view, message);
        } else {
            processRaw(view, message);
        }
    }

    private void processRaw(View view, MobWriteMessage mobWriteMessage) {
        view.setShadow(mobWriteMessage.getPayload());
        view.setShadowClientVersion(mobWriteMessage.getClientVersion());
        view.setShadowServerVersion(mobWriteMessage.getServerVersion());
        view.setBackupShadow(view.getShadow());
        view.setBackUpShadowServerVersion(view.getShadowServerVersion());
        view.getEditStack().clear();
    }

    private void processDelta(View view, MobWriteMessage mobWriteMessage) {
        if (view.getShadowServerVersion() == mobWriteMessage.getServerVersion()) {
            LinkedList<diff_match_patch.Diff> diffs = diffMatchPatch.diff_fromDelta(view.getShadow(), mobWriteMessage.getPayload());
            view.incrementShadowClientVersion();
            synchronized (view.getDocument()) {
                LinkedList<diff_match_patch.Patch> patches = diffMatchPatch.patch_make(view.getShadow(), diffs);
                view.setShadow(diffMatchPatch.diff_text2(diffs));
                view.setBackupShadow(view.getShadow());
                view.setBackUpShadowServerVersion(view.getShadowServerVersion());
                Document doc = view.getDocument();
                doc.setText(diffMatchPatch.patch_apply(patches, doc.getText())[0].toString());
            }
        } else {
            // todo: log error
            System.out.println("Shadow version mismatch detected");
        }
    }

    /**
     * @param view
     * @param mobWriteMessage
     * @return
     */
    public String generateDiffs(View view, MobWriteMessage mobWriteMessage) {
        String masterText = view.getDocument().getText();
        String command;
        if (view.isDeltaOk()) {
            LinkedList<diff_match_patch.Diff> diffs = diffMatchPatch.diff_main(view.getShadow(), masterText);
            String diffString = diffMatchPatch.diff_toDelta(diffs);
            command = String.format("d:%d:%s\n", view.getShadowServerVersion(), diffString);
        } else {
            // server could not parse client's delta, sent raw response back
            command = String.format("R:%d:%s\n", view.getShadowServerVersion(), masterText);
        }
        view.getEditStack().add(new View.EditStackElement(view.getShadowServerVersion(), command));
        view.incrementShadowServerVersion();
        view.setShadow(masterText);
        StringBuilder builder = new StringBuilder();
        builder.append("f:").append(view.getShadowClientVersion())
                .append(':').append(mobWriteMessage.getDocumentName()).append('\n');
        for (View.EditStackElement element : view.getEditStack()) {
            builder.append(element.edit);
        }
        return builder.append('\n').toString();
    }
}
