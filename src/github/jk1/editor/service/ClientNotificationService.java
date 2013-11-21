package github.jk1.editor.service;

import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import org.springframework.stereotype.Service;

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
public class ClientNotificationService {

    private static final Logger LOGGER = Logger.getLogger(ClientNotificationService.class.getName());

    private ChannelService channelService = ChannelServiceFactory.getChannelService();
    private Set<String> channelTokens = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

    public enum MessageKey {UPDATE_LIST, UPDATE_DOCUMENT}

    /**
     * Sends a message to all connected clients
     *
     * @param key message to be sent
     */
    public void broadcast(MessageKey key, BroadcastFilter filter) {
        for (String token : channelTokens) {
            if (filter.broadcastToChannel(token)) {
                channelService.sendMessage(new ChannelMessage(token, key.toString()));
            }
        }
    }

    /**
     * Generates unique string token for
     *
     * @return newly generated
     */
    public String createAndRegisterToken() {
        String token = channelService.createChannel(UUID.randomUUID().toString());
        channelTokens.add(token);
        LOGGER.info("New channel token created: " + token);
        return token;
    }

    public static interface BroadcastFilter {
        boolean broadcastToChannel(String channelToken);
    }

    public static final BroadcastFilter BROADCAST_TO_ALL = new BroadcastFilter() {
        @Override
        public boolean broadcastToChannel(String channelToken) {
            return true;
        }
    };
}
