package github.jk1.editor.service;

import github.jk1.editor.Document;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
@Repository
public class DocumentDao {

    private Map<Document.Header, Document> documents = new ConcurrentHashMap<>();
    private AtomicInteger idGenerator = new AtomicInteger(0);

    public DocumentDao() {
        String name = "Sample document";
        Document.Header header = new Document.Header(idGenerator.incrementAndGet(), name);
        documents.put(header, new Document(header));
    }

    public Document getDocument(Integer id) {
        return documents.get(new Document.Header(id));
    }

    public Collection<Document.Header> getAllDocumentHeaders() {
        return documents.keySet();
    }

    public Document.Header createDocument(String name){
        //name doesn't need to be unique, so no synchronization is necessary
        Document.Header header = new Document.Header(idGenerator.incrementAndGet(), name);
        documents.put(header, new Document(header));
        return header;
    }
}
