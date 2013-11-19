package github.jk1.editor.web;

import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

/**
 * Renders main (and the only) application page
 *
 * @author Evgeny Naumenko
 */
@Controller
public class MainPageController {

    private ChannelService channelService = ChannelServiceFactory.getChannelService();
    private UserService userService = UserServiceFactory.getUserService();

    @RequestMapping(value = {"/index", "/"}, method = RequestMethod.GET)
    public ModelAndView getEditorPage(HttpServletRequest request) {
        String token = channelService.createChannel(UUID.randomUUID().toString());
        ModelAndView mav = new ModelAndView("editor");
        mav.addObject("logoutUrl", userService.createLogoutURL(request.getRequestURI()));
        mav.addObject("token", token);
        return mav;
    }
}
