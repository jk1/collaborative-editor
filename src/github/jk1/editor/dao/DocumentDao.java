package github.jk1.editor.dao;

import github.jk1.editor.model.Document;
import github.jk1.editor.model.DocumentHeader;
import name.fraser.neil.plaintext.StandardBreakScorer;
import name.fraser.neil.plaintext.diff_match_patch;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * Basic in-memory document repository implementation.
 * For somewhat more production-like persistent repository is required
 * not to keep all the documents in memory.
 *
 * @author Evgeny Naumenko
 */
@Repository
public class DocumentDao {

    private static final Logger LOGGER = Logger.getLogger(DocumentDao.class.getName());

    private diff_match_patch diffMatchPatch = new diff_match_patch(new StandardBreakScorer());

    private Map<DocumentHeader, Document> documents = new ConcurrentHashMap<>();
    private AtomicInteger idGenerator = new AtomicInteger(0);

    public DocumentDao() {
        //init storage with one default document for demo purposes
        String name = "Sample document";
        DocumentHeader header = new DocumentHeader(idGenerator.incrementAndGet(), name);
        documents.put(header, new Document(diffMatchPatch, header));
    }

    public Document getDocument(Integer id) {
        return documents.get(new DocumentHeader(id));
    }

    public Collection<DocumentHeader> getAllDocumentHeaders() {
        return documents.keySet();
    }

    public DocumentHeader createDocument(String name){
        //name doesn't need to be unique, so no synchronization is necessary
        DocumentHeader header = new DocumentHeader(idGenerator.incrementAndGet(), name);
        documents.put(header, new Document(diffMatchPatch, header));
        LOGGER.info("New document created: " + header);
        return header;
    }
}
