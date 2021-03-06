package github.jk1.editor.service;

import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import github.jk1.editor.model.Document;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Serves GAE channel interaction from Java side. Mostly used
 * to push notifications from server to the clients
 *
 * @author Evgeny Naumenko
 * @see <a href="https://developers.google.com/appengine/docs/java/channel/">GAE Channel API</a>
 */
@Service
public class ClientChannelService {

    private static final Logger LOGGER = Logger.getLogger(ClientChannelService.class.getName());
    private ChannelService channelService = ChannelServiceFactory.getChannelService();
    private Set<String> channelTokens = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

    public static enum MessageKey {UPDATE_LIST, UPDATE_DOCUMENT}

    /**
     * Generates unique string token for document editor instance.
     * Single user may own multiple tokens, one for every browser window opened.
     * This token will not be a subject of broadcast until it is registered.
     *
     * @return newly generated unique token
     */
    public ChannelTokenCredentials createToken() {
        String clientId = UUID.randomUUID().toString();
        String token = channelService.createChannel(clientId, 24 * 60 - 1); // 1 day
        LOGGER.info("New channel token created: " + token);
        return new ChannelTokenCredentials(token, clientId);
    }

    /**
     * Register token for update message broadcasts
     */
    public void registerToken(String token) {
        LOGGER.info("New channel token registered: " + token);
        channelTokens.add(token);
    }

    /**
     * Removes token from update broadcast list
     */
    public void deleteToken(String token) {
        channelTokens.remove(token);
        LOGGER.info("Channel token removed: " + token);
    }

    /**
     * Sends a message to all connected clients that document
     * list has been changed and should be reloaded
     */
    public void broadcastDocumentListUpdate() {
        this.broadcast(channelTokens, MessageKey.UPDATE_LIST);
    }

    /**
     * Informs all connected clients, that document text has benn
     * changed and sync cycle is necessary
     *
     * @param document    updated document
     * @param authorToken update author
     */
    public void broadcastDocumentUpdate(Document document, String authorToken) {
        Collection<String> subscribers = document.getEditors();
        subscribers.remove(authorToken);
        this.broadcast(subscribers, MessageKey.UPDATE_DOCUMENT);
    }

    private void broadcast(Iterable<String> tokens, MessageKey key) {
        for (String token : tokens) {
            channelService.sendMessage(new ChannelMessage(token, key.toString()));
        }
    }

    public static class ChannelTokenCredentials{
        public final String token;
        public final String clientId;

        private ChannelTokenCredentials(String token, String clientId) {
            this.token = token;
            this.clientId = clientId;
        }
    }
}
