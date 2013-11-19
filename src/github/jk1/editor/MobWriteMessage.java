package github.jk1.editor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;

/**
 *
 *
 * @author Evgeny Naumenko
 * @see <a href="https://code.google.com/p/google-mobwrite/wiki/Protocol">MobWrite protocol reference</a>
 */
public class MobWriteMessage {

    public static enum Mode {DELTA, RAW}

    private String documentName;
    private String editorId;
    private Mode mode;
    private String payload;
    private int serverVersion;
    private int clientVersion;

    public MobWriteMessage(InputStream stream) throws IOException {
        //todo: support several d: lines
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            line = URLDecoder.decode(line, "UTF-8").substring(2); // strip "q="
            for (String command : line.split("\n")) {
                if (!command.isEmpty()) {
                    char name = command.substring(0, 1).toLowerCase().charAt(0);
                    String value = command.substring(2);
                    if ('u' == name) {
                        editorId = value;
                    } else {
                        int div = value.indexOf(':');
                        if (div == -1) {
                            continue; // line seems to be corrupted, skip it
                        }
                        // Parse out a version number for file, delta or raw.
                        if ('f' == name) {
                            serverVersion = Integer.parseInt(value.substring(0, div));
                            documentName = value.substring(div + 1);
                        } else if ('r' == name) {
                            mode = MobWriteMessage.Mode.RAW;
                            clientVersion = Integer.parseInt(value.substring(0, div));
                            payload = value.substring(div + 1); // strip colon
                        } else if ('d' == name) {
                            mode = MobWriteMessage.Mode.DELTA;
                            clientVersion = Integer.parseInt(value.substring(0, div));
                            payload = value.substring(div + 1); // strip colon
                        }
                    }
                }
            }
        }
    }

    public String getDocumentName() {
        return documentName;
    }

    public String getEditorId() {
        return editorId;
    }

    public Mode getMode() {
        return mode;
    }

    public String getPayload() {
        return payload;
    }

    public int getServerVersion() {
        return serverVersion;
    }

    public int getClientVersion() {
        return clientVersion;
    }

    @Override
    public String toString() {
        String template = "[Username: %s, Document: %s ,Mode: %s, client: %d, server: %d, payload: %s]";
        return String.format(template, editorId, documentName, mode, clientVersion, serverVersion, payload);
    }
}
