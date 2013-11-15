package github.jk1.editor.web;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URLDecoder;

/**
 *
 */
@Controller("/document")
public class DocumentController {

    private UserService userService = UserServiceFactory.getUserService();

    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView getDocument(HttpServletRequest request) {
        ModelAndView mav = new ModelAndView("editor");
        mav.addObject("logoutUrl", userService.createLogoutURL(request.getRequestURI()));
        return mav;
    }

    @RequestMapping(method = RequestMethod.POST)
    public void postChanges(Reader reader) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(reader);
        String line;
        while ((line = bufferedReader.readLine()) != null)
            System.out.println(URLDecoder.decode(line));
    }
}
