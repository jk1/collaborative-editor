package github.jk1.editor.web;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Enforces GAE-based authentication with google accounts
 *
 * @author Evgeny Naumenko
 */
@Component
public class AuthenticationFilter implements Filter {

    private static final String[] ANON_ACCESS_TEMPLATES = {
            "login",
            "/_ah/",   // call to /_ah/channel/*/ is made by an internal Google Channel server with no user associated with it.
            "/css/",
            "/js/",
            "/img/"
    };

    @Autowired
    private UserService userService;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // noop
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        UserService userService = UserServiceFactory.getUserService();
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        String requestUri = request.getRequestURI();
        if (this.isAccessGranted(requestUri)) {
            chain.doFilter(request, response);
        } else {
            String loginUrl = userService.createLoginURL(requestUri);
            response.sendRedirect(loginUrl);
        }
    }

    private boolean isAccessGranted(String uri) {
        if (userService.isUserLoggedIn()) {
            return true;
        }
        for (String template : ANON_ACCESS_TEMPLATES) {
            if (uri.contains(template)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void destroy() {
        //noop
    }
}
