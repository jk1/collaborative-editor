package github.jk1.editor.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represent diff-posting request from a client or a server response.
 * This class is able to perform serialization routine from Java objects
 * to MobWrite protocol messages and vise versa.
 *
 * todo: split into request\response subclasses
 * todo: introduce separate command handlers
 *
 * @author Evgeny Naumenko
 * @see <a href="https://code.google.com/p/google-mobwrite/wiki/Protocol">MobWrite protocol reference</a>
 */
public class MobWriteMessage {

    private String documentName;
    private String token;
    private int version;
    private int cursorPosition;
    private boolean deltaEmpty = true;
    private List<Diff> diffs = new ArrayList<>();
    private List<Integer> responseCursors = new ArrayList<>();

    private MobWriteMessage() {
    }

    public MobWriteMessage(String documentName, String token, int version) {
        this.documentName = documentName;
        this.token = token;
        this.version = version;
    }

    /**
     * @param stream raw client request
     * @return parsed client message for further processing
     * @throws IOException if client request is not parable
     */
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
            } else if ('c' == name) {
                message.cursorPosition = Integer.parseInt(value.substring(div + 1));
            } else {
                int clientVersion = Integer.parseInt(value.substring(0, div));
                String payload = value.substring(div + 1); // strip colon
                message.diffs.add(new Diff(name, clientVersion, payload));
            }
        }
    }

    /**
     * @return string representation of the server's MobWrite response to be sent back to the client
     */
    public String asString() {
        StringBuilder builder = new StringBuilder();
        builder.append("f:").append(version).append(':').append(documentName).append('\n');
        for (Diff element : diffs) {
            builder.append(element);
        }
        Collections.sort(responseCursors);
        for (Integer cursor : responseCursors) {
            builder.append("c:").append(cursor).append("\n");
        }
        return builder.append('\n').toString();
    }

    public void setDeltaEmpty(boolean deltaEmpty) {
        this.deltaEmpty = deltaEmpty;
    }

    public boolean isDeltaEmpty(){
        return deltaEmpty;
    }

    public String getDocumentName() {
        return documentName;
    }

    /**
     * @return unique token of editor instance, which generates the request, same as channel token
     */
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

    public void setResponseCursors(List<Integer> responseCursors) {
        this.responseCursors = responseCursors;
    }

    public int getCursorPosition() {
        return cursorPosition;
    }

    @Override
    public String toString() {
        String template = "[Document: %s, server: %d, diffs: %s, editor name: %s, cursor position: %d]";
        return String.format(template, documentName, version, diffs, token, cursorPosition);
    }
}
