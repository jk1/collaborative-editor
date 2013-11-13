package github.jk1.editor.web;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 *
 */
public class MainPageServlet extends HttpServlet {
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        UserService userService = UserServiceFactory.getUserService();
        User user = userService.getCurrentUser();

        resp.setContentType("text/html");
        resp.getWriter().println("<h2>Integrating Google user account</h2>");

        if (user != null) {
            resp.getWriter().println("Welcome, " + user.getNickname());
            resp.getWriter().println(
                    "<a href='"
                            + userService.createLogoutURL(req.getRequestURI())
                            + "'> LogOut </a>");

        } else {
            resp.getWriter().println(
                    "Please <a href='"
                            + userService.createLoginURL(req.getRequestURI())
                            + "'> LogIn </a>");

        }
    }
}
