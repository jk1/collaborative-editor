package github.jk1.editor.web;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import github.jk1.editor.service.CommunicationService;
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
    private CommunicationService communicationService;

    @Autowired
    public MainPageController(CommunicationService communicationService) {
        this.communicationService = communicationService;
    }

    @RequestMapping(value = {"/index", "/"}, method = RequestMethod.GET)
    public ModelAndView getEditorPage(HttpServletRequest request) {
        String token = communicationService.createAndRegisterToken();
        ModelAndView mav = new ModelAndView("editor");
        mav.addObject("logoutUrl", userService.createLogoutURL(request.getRequestURI()));
        mav.addObject("token", token);
        return mav;
    }
}
