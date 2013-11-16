package github.jk1.editor.service;

import github.jk1.editor.Document;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Evgeny Naumenko
 */
@Service
public class DocumentService {

    private Map<Document.Header, Document> documents = new ConcurrentHashMap<>();
    private AtomicInteger idGenerator = new AtomicInteger(0);

    public DocumentService() {
        String name = "Sample document";
        Document.Header header = new Document.Header(idGenerator.incrementAndGet(), name);
        documents.put(header, new Document(header, "Default text"));
    }

    public Document getDocument(Integer id) {
        return documents.get(new Document.Header(id));
    }

    public Collection<Document.Header> getAllDocumentHeaders() {
        return documents.keySet();
    }

    public Document.Header createDocument(String name){
        Document.Header header = new Document.Header(idGenerator.incrementAndGet(), name);
        documents.put(header, new Document(header));
        return header;
    }
}
