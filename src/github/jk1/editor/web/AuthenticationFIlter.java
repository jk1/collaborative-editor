package github.jk1.editor.web;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Enforces GAE-based authentication using google accounts
 *
 * @author Evgeny Naumenko
 */
@Component
public class AuthenticationFilter implements Filter {

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
        if (userService.isUserLoggedIn() || requestUri.contains("login")) {
            chain.doFilter(request, response);
        } else {
            String loginUrl = userService.createLoginURL(requestUri);
            response.sendRedirect(loginUrl);
        }
    }

    @Override
    public void destroy() {
        //noop
    }
}
