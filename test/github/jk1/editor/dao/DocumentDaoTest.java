package github.jk1.editor.dao;

import github.jk1.editor.model.Document;
import github.jk1.editor.model.DocumentHeader;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.*;

/**
 * @author Evgeny Naumenko
 */
public class DocumentDaoTest {

    private DocumentDao dao;

    @Before
    public void setUp() {
        dao = new DocumentDao();
    }

    @Test
    public void testGetDocument() {
        Document document = dao.getDocument(1);

        assertEquals("Sample document", document.getTitle());
    }

    @Test
    public void testCreateDocument() {
        DocumentHeader header = dao.createDocument("name");

        Document document = dao.getDocument(header.getId());

        assertEquals(header.getTitle(), document.getTitle());
        assertEquals(header.getId(), document.getId());
    }

    @Test
    public void testGetAllDocumentHeaders() {
        DocumentHeader header = dao.createDocument("name");

        Collection documentHeaders = dao.getAllDocumentHeaders();

        assertTrue(documentHeaders.contains(header));
    }

    @Test(expected = NotFoundException.class)
    public void testDocumentNotFound() {
        dao.getDocument(Integer.MAX_VALUE);
    }
}
