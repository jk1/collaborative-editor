package github.jk1.editor.web;

import github.jk1.editor.Document;
import github.jk1.editor.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URLDecoder;
import java.util.Collection;

/**
 *
 */
@Controller
public class DocumentController {

    private DocumentService documentService;

    @Autowired
    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @RequestMapping("/headers")
    @ResponseBody
    public Collection<Document.Header> getAllDocumentNames() {
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
    public void postChanges(@PathVariable Integer id, Reader reader) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(reader);
        String line;
        while ((line = bufferedReader.readLine()) != null)
            System.out.println(URLDecoder.decode(line));
    }
}
