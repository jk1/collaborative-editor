package github.jk1.editor.web;

import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.google.appengine.api.users.UserServiceFactory;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class EditorServlet extends HttpServlet {
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // Game creation, user sign-in, etc. omitted for brevity.
        String userId = UserServiceFactory.getUserService().getCurrentUser().getUserId();

        ChannelService channelService = ChannelServiceFactory.getChannelService();

        // The 'Game' object exposes a method which creates a unique string based on the game's key
        // and the user's id.
        //String token = channelService.createChannel(game.getChannelKey(userId));

        // Index is the contents of our index.html resource, details omitted for brevity.
        //index = index.replaceAll("\\{\\{ token \\}\\}", token);

        resp.setContentType("text/html");
        //resp.getWriter().write(index);
    }
}