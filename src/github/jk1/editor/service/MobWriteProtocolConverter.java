package github.jk1.editor.service;

import github.jk1.editor.Message;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author Evgeny Naumenko
 * @see <a href="https://code.google.com/p/google-mobwrite/wiki/Protocol">MobWrite protocol reference</a>
 */
@Service
public class MobWriteProtocolConverter {

    public Message parseRequest(InputStream stream) throws IOException {
        Message message = new Message();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            char name = line.substring(0, 1).toLowerCase().charAt(0);
            String value = line.substring(2);
            int div = value.indexOf(':');
            if (div == -1) {
                continue; // line seems to be corrupted, skip it
            }
            // Parse out a version number for file, delta or raw.
            if ('f' == name) {
                message.setServerVersion(Integer.parseInt(value.substring(0, div)));
            } else if ('r' == name) {
                message.setMode(Message.Mode.RAW);
                message.setPayload(value.substring(div + 1)); // strip colon
            }
            if ('d' == name) {
                message.setMode(Message.Mode.DELTA);
                message.setPayload(value.substring(div + 1)); // strip colon
            }
        }
        return message;
    }

    public String createResponse(Message message, int documentId) {
        StringBuilder builder = new StringBuilder();
        builder.append("f:").append(message.getServerVersion()).append(":").append(documentId).append("\n");
        builder.append("d:").append(message.getClientVersion()).append(":").append(message.getPayload()).append("\n");
        builder.append("\n");
        return builder.toString();
    }
}
