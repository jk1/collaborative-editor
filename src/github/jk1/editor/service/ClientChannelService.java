package github.jk1.editor.service;

import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelService;
import github.jk1.editor.model.Document;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private ChannelService channelService;
    private Set<String> channelTokens = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

    private enum MessageKey {UPDATE_LIST, UPDATE_DOCUMENT}

    /**
     * Generates unique string token for document editor instance.
     * Single user may own multiple tokens, one for every browser window opened.
     *
     * @return newly generated unique token string
     */
    public String createToken() {
        String token = channelService.createChannel(UUID.randomUUID().toString());
        LOGGER.info("New channel token created: " + token);
        return token;
    }

    public void registerToken(String token) {
        LOGGER.info("New channel token registered: " + token);
        channelTokens.add(token);
    }

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
        Collection<String> subscribers = document.getSubscribers();
        subscribers.remove(authorToken);
        this.broadcast(subscribers, MessageKey.UPDATE_DOCUMENT);
    }

    private void broadcast(Iterable<String> tokens, MessageKey key) {
        for (String token : tokens) {
            channelService.sendMessage(new ChannelMessage(token, key.toString()));
        }
    }
}
