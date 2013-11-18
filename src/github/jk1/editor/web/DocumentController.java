package github.jk1.editor.web;

import github.jk1.editor.Document;
import github.jk1.editor.Message;
import github.jk1.editor.View;
import github.jk1.editor.service.DocumentService;
import github.jk1.editor.service.MobWriteProtocolConverter;
import name.fraser.neil.plaintext.StandardBreakScorer;
import name.fraser.neil.plaintext.diff_match_patch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedList;

import static github.jk1.editor.View.*;
import static name.fraser.neil.plaintext.diff_match_patch.Diff;
import static name.fraser.neil.plaintext.diff_match_patch.Patch;

/**
 * Rest-like controller for document operations
 *
 * @author Evgeny Naumenko
 */
@Controller
public class DocumentController {

    private DocumentService documentService;
    private MobWriteProtocolConverter converter;
    private diff_match_patch diffMatchPatch = new diff_match_patch(new StandardBreakScorer());

    @Autowired
    public DocumentController(DocumentService documentService, MobWriteProtocolConverter converter) {
        this.documentService = documentService;
        this.converter = converter;
    }

    @RequestMapping("/headers")
    @ResponseBody
    public Collection<Document.Header> getAllDocumentHeaders() {
        return documentService.getAllDocumentHeaders();
    }

    @RequestMapping("/document/{id}")
    @ResponseBody
    public Document getDocument(@PathVariable Integer id) {
        return documentService.getDocument(id);
    }

    @RequestMapping(value = "/document", method = RequestMethod.POST)
    @ResponseBody
    public Document.Header createDocument(@RequestParam String name) {
        return documentService.createDocument(name);
    }

    @RequestMapping(value = "/document/{id}", method = RequestMethod.POST)
    @ResponseBody
    public String postChanges(@PathVariable Integer id, InputStream stream) throws IOException {
        Document document = documentService.getDocument(id);
        Message message = converter.parseRequest(stream);
        System.out.println(message);
        View view = document.getView(message.getUserName());
        view.setDeltaOk(true);
        if (message.getServerVersion() != view.getShadowServerVersion()
                || message.getServerVersion() == view.getBackUpShadowServerVersion()) {
            //client missed the last update, rollback the shadow
            view.rollback();
        }
        view.dropObsoleteEditStackRecords(message.getServerVersion());
        if (message.getMode() == Message.Mode.DELTA) {
            processDelta(view, message);
        } else {
            processRaw(view, message);
        }
        return this.generateDiffs(view, message);
    }

    private void processRaw(View view, Message message) {
        view.setShadow(message.getPayload());
        view.setShadowClientVersion(message.getClientVersion());
        view.setShadowServerVersion(message.getServerVersion());
        view.setBackupShadow(view.getShadow());
        view.setBackUpShadowServerVersion(view.getShadowServerVersion());
        view.getEditStack().clear();
    }

    private void processDelta(View view, Message message) {
        if (view.getShadowServerVersion() == message.getServerVersion()) {
            LinkedList<Diff> diffs = diffMatchPatch.diff_fromDelta(view.getShadow(), message.getPayload());
            view.incrementShadowClientVersion();
            synchronized (view.getDocument()) {
                LinkedList<Patch> patches = diffMatchPatch.patch_make(view.getShadow(), diffs);
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

    private String generateDiffs(View view, Message message) {
        String masterText = view.getDocument().getText();
        String command;
        if (view.isDeltaOk()) {
            LinkedList<Diff> diffs = diffMatchPatch.diff_main(view.getShadow(), masterText);
            String diffString = diffMatchPatch.diff_toDelta(diffs);
            command = String.format("d:%d:%s\n", view.getShadowServerVersion(), diffString);
        } else {
            // server could not parse client's delta, sent raw response back
            command = String.format("R:%d:%s\n", view.getShadowServerVersion(), masterText);
        }
        view.getEditStack().add(new EditStackElement(view.getShadowServerVersion(), command));
        view.incrementShadowServerVersion();
        view.setShadow(masterText);
        StringBuilder builder = new StringBuilder();
        builder.append("f:").append(view.getShadowClientVersion())
                .append(":").append(message.getDocumentName()).append("\n");
        for (EditStackElement element : view.getEditStack()) {
            builder.append(element.edit);
        }
        return builder.toString();
    }
}
