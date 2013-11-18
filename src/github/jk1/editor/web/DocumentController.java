package github.jk1.editor.web;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
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
    private UserService userService = UserServiceFactory.getUserService();
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
        System.out.println("Document id:" + id);
        Document document = documentService.getDocument(id);
        View view = document.getView(userService.getCurrentUser().getUserId());
        view.setDeltaOk(true);

        Message message = converter.parseRequest(stream);
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
        return converter.createResponse(message, id);
    }

    private void processRaw(View view, Message message) {
        view.setShadow(message.getPayload());
        view.setShadowClientVersion(message.getClientVersion());
        view.setShadowServerVersion(message.getServerVersion());
        view.setBackupShadow(view.getShadow());
        view.setBackUpShadowServerVersion(view.getShadowServerVersion());
        view.getEditStack().clear();
        //lock
        view.getDocument().setText(message.getPayload());
        //unlock
    }

    private void processDelta(View view, Message message) {
        if (view.getShadowServerVersion() == message.getServerVersion()) {
            LinkedList<Diff> diffs = diffMatchPatch.diff_fromDelta(view.getShadow(), message.getPayload());
            view.incrementShadowServerVersion();
            //lock
            LinkedList<Patch> patches = diffMatchPatch.patch_make(view.getShadow(), diffs);
            view.setShadow(diffMatchPatch.diff_text2(diffs));
            view.setBackupShadow(view.getShadow());
            view.setBackUpShadowServerVersion(view.getShadowServerVersion());

            Document doc = view.getDocument();
            doc.setText(diffMatchPatch.patch_apply(patches, doc.getText())[0].toString());
            //unlock
        } else {
            // todo: log error
        }
    }

    private void generateDiffs(View view) {

    }
}
