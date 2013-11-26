package github.jk1.editor.web;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import github.jk1.editor.service.ClientChannelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

/**
 * Renders main (and the only) application page
 *
 * @author Evgeny Naumenko
 */
@Controller
public class MainPageController {

    private UserService userService = UserServiceFactory.getUserService();

    @Autowired
    private ClientChannelService clientChannelService;

    @RequestMapping(value = {"/index", "/"}, method = RequestMethod.GET)
    public ModelAndView getEditorPage(HttpServletRequest request) {
        ClientChannelService.ChannelTokenCredentials credentials = clientChannelService.createToken();
        ModelAndView mav = new ModelAndView("editor");
        mav.addObject("logoutUrl", userService.createLogoutURL(request.getRequestURI()));
        mav.addObject("token", credentials.token);
        mav.addObject("clientId", credentials.clientId);
        return mav;
    }
}
