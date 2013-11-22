package github.jk1.editor.web;

import com.google.appengine.api.channel.ChannelPresence;
import com.google.appengine.api.channel.ChannelService;
import github.jk1.editor.service.ClientChannelService;
import github.jk1.editor.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Handles connects and disconnects of client's channels.
 * These events trigger user view data cleanup to free the memory when
 * user editing session is over
 *
 * @author Evgeny Naumenko
 */
@Controller
@RequestMapping("/_ah/channel")
public class ChannelMonitoringController {

    @Autowired
    private ChannelService channelService;
    @Autowired
    private ClientChannelService clientChannelService;
    @Autowired
    private DocumentService documentService;

    @RequestMapping(value="/connected/", method = RequestMethod.POST)
    @ResponseBody
    public void connected(HttpServletRequest request) throws IOException {
        clientChannelService.registerToken(this.parseToken(request));
    }

    @RequestMapping(value="/disconnected/", method = RequestMethod.POST)
    @ResponseBody
    public void disconnected(HttpServletRequest request) throws IOException {
        String token = this.parseToken(request);
        clientChannelService.deleteToken(token);
        documentService.deleteView(token);
    }

    private String parseToken(HttpServletRequest request) throws IOException {
        ChannelPresence presence = channelService.parsePresence(request);
        return presence.clientId();
    }
}
