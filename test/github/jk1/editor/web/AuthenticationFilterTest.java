package github.jk1.editor.web;

import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.ServletException;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * @author Evgeny Naumenko
 */
public class AuthenticationFilterTest {

    private LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalUserServiceTestConfig());
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain chain;
    private static final String REDIRECT_URL = "/_ah/login?continue=";

    private AuthenticationFilter filter;

    @Before
    public void setUp() {
        helper.setUp();
        filter = new AuthenticationFilter();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        chain = new MockFilterChain();
    }

    @After
    public void tearDown() {
        helper.tearDown();
    }

    @Test
    public void testLoggedIn() throws IOException, ServletException {
        helper.setEnvIsLoggedIn(true);

        filter.doFilter(request, response, chain);

        assertNull(response.getRedirectedUrl());
        assertNotNull(chain.getRequest());
    }

    @Test
    public void testNotLoggedIn() throws IOException, ServletException {
        helper.setEnvIsLoggedIn(false);

        filter.doFilter(request, response, chain);

        assertEquals(REDIRECT_URL, response.getRedirectedUrl());
    }

    @Test
    public void testRequestStatic() throws IOException, ServletException {
        helper.setEnvIsLoggedIn(false);
        request.setRequestURI("/img/icon.png");

        filter.doFilter(request, response, chain);

        assertNull(response.getRedirectedUrl());
        assertNotNull(chain.getRequest());
    }

    @Test
    public void testChannelCommunication() throws IOException, ServletException {
        helper.setEnvIsLoggedIn(false);
        request.setRequestURI("/_ah/channel/request");

        filter.doFilter(request, response, chain);

        assertNull(response.getRedirectedUrl());
        assertNotNull(chain.getRequest());
    }
}
