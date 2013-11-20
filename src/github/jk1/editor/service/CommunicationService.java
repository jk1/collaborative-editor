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
 * @author Evgeny Naumenko
 * @see <a href="https://code.google.com/p/google-mobwrite/wiki/Protocol">MobWrite protocol reference</a>
 */
@Service
public class CommunicationService {

    private static final Logger LOGGER = Logger.getLogger(CommunicationService.class.getName());
    private ChannelService channelService = ChannelServiceFactory.getChannelService();
    private Set<String> channelTokens = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

    /**
     * Sends a message to all connected clients except the author
     *
     * @param authorId
     * @param message
     */
    public void broadcast(String authorId, String message) {
        for (String token : channelTokens){
           if (!token.equals(authorId)){
               channelService.sendMessage(new ChannelMessage(token, message));
           }
        }
    }

    /**
     *
     * @return
     */
    public String createAndRegisterToken() {
        String token = channelService.createChannel(UUID.randomUUID().toString());
        channelTokens.add(token);
        LOGGER.info("New channel token created: " + token);
        return token;
    }
}
