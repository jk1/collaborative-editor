package github.jk1.editor.dao;

import github.jk1.editor.model.Document;
import github.jk1.editor.model.DocumentHeader;
import name.fraser.neil.plaintext.diff_match_patch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Map;
import java.util.TreeSet;
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

    @Autowired
    private diff_match_patch diffMatchPatch;

    private Map<DocumentHeader, Document> documents = new ConcurrentHashMap<>();
    private AtomicInteger idGenerator = new AtomicInteger(0);

    /**
     *
     * @param id unique document identifier
     * @throws NotFoundException if no document matches id given
     * @return document matching id given
     */
    public Document getDocument(Integer id) {
        Document document = documents.get(new DocumentHeader(id));
        if (document == null) {
            String message = "Unable to find requested document with id " + id;
            LOGGER.warning(message);
            throw new NotFoundException(message);
        } else {
            return document;
        }
    }

    public Collection<DocumentHeader> getAllDocumentHeaders() {
        return new TreeSet<>(documents.keySet());
    }

    public DocumentHeader createDocument(String name) {
        //name doesn't need to be unique, so no synchronization is necessary
        DocumentHeader header = new DocumentHeader(idGenerator.incrementAndGet(), name);
        documents.put(header, new Document(diffMatchPatch, header));
        LOGGER.info("New document created: " + header);
        return header;
    }
}
