package github.jk1.editor.service;

import github.jk1.editor.dao.DocumentDao;
import github.jk1.editor.model.Document;
import github.jk1.editor.model.DocumentHeader;
import github.jk1.editor.model.MobWriteRequest;
import github.jk1.editor.model.View;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Evgeny Naumenko
 */
@RunWith(MockitoJUnitRunner.class)
public class DocumentServiceTest {

    @Mock
    private DocumentDao dao;
    @Mock
    private ClientChannelService channelService;
    @Mock
    private MobWriteRequest request;
    @Mock
    private Document document;
    @Mock
    private View view;

    private DocumentService service;

    @Before
    public void setUp() {
        service = new DocumentService(channelService, dao);
        when(document.getView(anyString())).thenReturn(view);
    }

    @Test
    public void testCreateDocument() {
        String name = "documentName";

        service.createDocument(name);

        verify(dao).createDocument(name);
        verify(channelService).broadcastDocumentListUpdate();
    }

    @Test
    public void testDeleteView() {
        String token = "token";
        String otherToken = "otherToken";
        DocumentHeader header = new DocumentHeader(1);
        Document document = new Document(null, header);
        document.getView(token);
        document.getView(otherToken);
        when(dao.getAllDocumentHeaders()).thenReturn(Collections.singleton(header));
        when(dao.getDocument(header.getId())).thenReturn(document);

        service.deleteView(token);

        assertEquals(1, document.getEditors().size());
        assertEquals(otherToken, document.getEditors().iterator().next());
    }

    @Test
    public void testApplyClientMessageWithDiff() {
        String token = "token";
        when(request.isDeltaEmpty()).thenReturn(false);
        when(request.getToken()).thenReturn(token);

        service.applyClientMessage(document, request);

        verify(channelService).broadcastDocumentUpdate(document, token);
    }

    @Test
    public void testApplyClientMessageWithoutDiff() {
        String token = "token";
        when(request.isDeltaEmpty()).thenReturn(true);
        when(request.getToken()).thenReturn(token);

        service.applyClientMessage(document, request);

        verify(channelService, never()).broadcastDocumentUpdate(document, token);
    }
}
