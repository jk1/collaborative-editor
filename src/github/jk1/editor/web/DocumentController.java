package github.jk1.editor.web;

import github.jk1.editor.Document;
import github.jk1.editor.MobWriteMessage;
import github.jk1.editor.View;
import github.jk1.editor.service.CommunicationService;
import github.jk1.editor.service.DocumentDao;
import github.jk1.editor.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import static github.jk1.editor.service.CommunicationService.MessageKey.UPDATE_DOCUMENT;
import static github.jk1.editor.service.CommunicationService.MessageKey.UPDATE_LIST;

/**
 * Rest-like controller for document operations
 *
 * @author Evgeny Naumenko
 */
@Controller
public class DocumentController {

    private DocumentDao documentDao;
    private DocumentService documentService;
    private CommunicationService communicationService;

    @Autowired
    public DocumentController(DocumentDao documentDao, DocumentService documentService,
                              CommunicationService communicationService) {
        this.documentDao = documentDao;
        this.documentService = documentService;
        this.communicationService = communicationService;
    }

    @RequestMapping("/headers")
    @ResponseBody
    public Collection<Document.Header> getAllDocumentHeaders() {
        return documentDao.getAllDocumentHeaders();
    }

    @RequestMapping(value = "/document", method = RequestMethod.POST)
    @ResponseBody
    public void createDocument(@RequestParam String name) {
        documentDao.createDocument(name);
        communicationService.broadcast(UPDATE_LIST, CommunicationService.BROADCAST_TO_ALL);
    }

    @RequestMapping(value = "/document/{id}", method = RequestMethod.POST)
    @ResponseBody
    public String postChanges(@PathVariable Integer id, InputStream stream) throws IOException {
        Document document = documentDao.getDocument(id);
        final MobWriteMessage message = new MobWriteMessage(stream);
        View view = document.getView(message.getEditorName());
        documentService.process(view, message);
        communicationService.broadcast(UPDATE_DOCUMENT, new CommunicationService.BroadcastFilter() { // todo: separate thread for speedup?
            @Override
            public boolean broadcastToChannel(String channelToken) {
                //to all but the diff author
                return !channelToken.equals(message.getEditorName());
            }
        });
        return documentService.generateDiffs(view, message);
    }


}
