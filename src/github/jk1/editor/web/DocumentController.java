package github.jk1.editor.web;

import github.jk1.editor.model.Document;
import github.jk1.editor.model.DocumentHeader;
import github.jk1.editor.model.MobWriteMessage;
import github.jk1.editor.dao.DocumentDao;
import github.jk1.editor.model.MobWriteRequest;
import github.jk1.editor.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

/**
 * Rest-like controller for document operations
 *
 * @author Evgeny Naumenko
 */
@Controller
public class DocumentController {

    @Autowired
    private DocumentDao documentDao;
    @Autowired
    private DocumentService documentService;

    @RequestMapping("/headers")
    @ResponseBody
    public Collection<DocumentHeader> getAllDocumentHeaders() {
        return documentDao.getAllDocumentHeaders();
    }

    @RequestMapping(value = "/document", method = RequestMethod.POST)
    @ResponseBody
    public void createDocument(@RequestParam String name) {
        documentService.createDocument(name);
    }

    @RequestMapping(value = "/document/{id}", method = RequestMethod.POST)
    @ResponseBody
    public String postChanges(@PathVariable Integer id, InputStream stream) throws IOException {
        Document document = documentDao.getDocument(id);
        MobWriteRequest message = new MobWriteRequest(stream);
        return documentService.applyClientMessage(document, message).asString();
    }

    @RequestMapping(value = "/document/dropview", method = RequestMethod.POST)
    @ResponseBody
    public void dropView(@RequestParam String token) throws IOException {
        documentService.deleteView(token);
    }
}
