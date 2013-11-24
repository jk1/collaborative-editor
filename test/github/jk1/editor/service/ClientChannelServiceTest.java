package github.jk1.editor.service;

import com.google.appengine.api.channel.dev.ChannelManager;
import com.google.appengine.tools.development.testing.LocalChannelServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import github.jk1.editor.model.Document;
import github.jk1.editor.model.DocumentHeader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Evgeny Naumenko
 */
public class ClientChannelServiceTest {

    private LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalChannelServiceTestConfig());
    private ChannelManager channelManager;

    private String token;
    private String connectionId;

    private ClientChannelService service;

    @Before
    public void setUp() {
        helper.setUp();
        channelManager = LocalChannelServiceTestConfig.getLocalChannelService().getChannelManager();
        service = new ClientChannelService();
        token = service.createToken();
        connectionId = channelManager.connectClient(token);
    }

    @After
    public void tearDown() {
        helper.tearDown();
    }

    @Test
    public void testIgnoreUnregisteredTokenOnBroadcast() {
        service.broadcastDocumentListUpdate();

        assertNotNull(token);
        assertNotEquals(token, "");
        String message = channelManager.getNextClientMessage(token, connectionId);
        assertNull(message);
    }

    @Test
    public void testBroadcastWithRegisteredToken() {
        service.registerToken(token);

        service.broadcastDocumentListUpdate();

        String message = channelManager.getNextClientMessage(token, connectionId);
        assertEquals(ClientChannelService.MessageKey.UPDATE_LIST.toString(), message);
    }

    @Test
    public void testDeleteToken() {
        service.registerToken(token);
        service.deleteToken(token);

        service.broadcastDocumentListUpdate();

        String message = channelManager.getNextClientMessage(token, connectionId);
        assertNull(message);
    }

    @Test
    public void testBroadcastDocumentUpdateToDocumentEditorsOnly() {
        service.registerToken(token);
        String editorToken = service.createToken();
        String editorConnectionId = channelManager.connectClient(editorToken);
        service.registerToken(editorToken);
        Document document = new Document(null, new DocumentHeader(1, ""));
        document.getView(editorToken);

        service.broadcastDocumentUpdate(document, token);

        String message = channelManager.getNextClientMessage(token, connectionId);
        assertNull(message);
        message = channelManager.getNextClientMessage(editorToken, editorConnectionId);
        assertEquals(ClientChannelService.MessageKey.UPDATE_DOCUMENT.toString(), message);
    }

    @Test
    public void testNoDocumentUpdateBroadcastToAuthor() {
        service.registerToken(token);
        String authorToken = service.createToken();
        String authorConnectionId = channelManager.connectClient(authorToken);
        service.registerToken(authorToken);
        Document document = new Document(null, new DocumentHeader(1, ""));
        document.getView(authorToken);
        document.getView(token);

        service.broadcastDocumentUpdate(document, authorToken);

        String message = channelManager.getNextClientMessage(token, connectionId);
        assertEquals(ClientChannelService.MessageKey.UPDATE_DOCUMENT.toString(), message);
        message = channelManager.getNextClientMessage(authorToken, authorConnectionId);
        assertNull(message);
    }
}
