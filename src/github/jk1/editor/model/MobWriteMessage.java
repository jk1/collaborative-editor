package github.jk1.editor.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Represent diff-posting request from a client or a server response.
 * This class is able to perform serialization routine from Java objects
 * to MobWrite protocol messages and vise versa.
 *
 * @author Evgeny Naumenko
 * @see <a href="https://code.google.com/p/google-mobwrite/wiki/Protocol">MobWrite protocol reference</a>
 */
public class MobWriteMessage {

    private String documentName;
    private String token;
    private int version;
    private List<Diff> diffs = new ArrayList<>();

    private MobWriteMessage() {
    }

    public MobWriteMessage(String documentName, String token, int version) {
        this.documentName = documentName;
        this.token = token;
        this.version = version;
    }

    public static MobWriteMessage fromStream(InputStream stream) throws IOException {
        MobWriteMessage message = new MobWriteMessage();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            line = URLDecoder.decode(line, "UTF-8");
            // mobWrite explicitly uses \n as a line delimiter regardless of browser or platform
            for (String command : line.split("\n")) {
                if (!command.isEmpty()) {
                    parseCommand(command, message);
                }
            }
        }
        return message;
    }

    private static void parseCommand(String command, MobWriteMessage message) {
        char name = command.substring(0, 1).toLowerCase().charAt(0);
        String value = command.substring(2);
        if ('u' == name) {
            message.token = value;
        } else {
            int div = value.indexOf(':');
            if (div == -1) {
                return; // line seems to be corrupted, skip it
            }
            if ('f' == name) {
                message.version = Integer.parseInt(value.substring(0, div));
                message.documentName = value.substring(div + 1);
            } else {
                int clientVersion = Integer.parseInt(value.substring(0, div));
                String payload = value.substring(div + 1); // strip colon
                message.diffs.add(new Diff(name, clientVersion, payload));
            }
        }
    }

    /**
     *
     * @return
     */
    public String asString() {
        StringBuilder builder = new StringBuilder();
        builder.append("f:").append(version).append(':').append(documentName).append('\n');
        for (Diff element : diffs) {
            builder.append(element);
        }
        return builder.append('\n').toString();
    }

    public String getDocumentName() {
        return documentName;
    }

    public String getToken() {
        return token;
    }

    public int getVersion() {
        return version;
    }

    public List<Diff> getDiffs() {
        return diffs;
    }

    public void addDiff(Diff diff) {
        diffs.add(diff);
    }

    @Override
    public String toString() {
        String template = "[Document: %s, server: %d, diffs: %s, editor name: %s]";
        return String.format(template, documentName, version, diffs, token);
    }
}
