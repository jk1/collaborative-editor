package github.jk1.editor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Represent diff-posting request from a client.
 *
 * @author Evgeny Naumenko
 * @see <a href="https://code.google.com/p/google-mobwrite/wiki/Protocol">MobWrite protocol reference</a>
 */
public class MobWriteMessage {


    private String documentName;
    private String editorName;
    private int serverVersion;
    private List<Diff> diffs = new ArrayList<>();

    public MobWriteMessage(InputStream stream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            line = URLDecoder.decode(line, "UTF-8");
            for (String command : line.split("\n")) {
                if (!command.isEmpty()) {
                    char name = command.substring(0, 1).toLowerCase().charAt(0);
                    String value = command.substring(2);
                    if ('u' == name) {
                        editorName = value;
                    } else {
                        int div = value.indexOf(':');
                        if (div == -1) {
                            continue; // line seems to be corrupted, skip it
                        }
                        // Parse out a version number for file, delta or raw.
                        if ('f' == name) {
                            serverVersion = Integer.parseInt(value.substring(0, div));
                            documentName = value.substring(div + 1);
                        } else {
                            int clientVersion = Integer.parseInt(value.substring(0, div));
                            String payload = value.substring(div + 1); // strip colon
                            diffs.add(new Diff(name, clientVersion, payload));
                        }
                    }
                }
            }
        }
    }

    public String getDocumentName() {
        return documentName;
    }

    public String getEditorName() {
        return editorName;
    }

    public int getServerVersion() {
        return serverVersion;
    }

    public List<Diff> getDiffs() {
        return diffs;
    }

    @Override
    public String toString() {
        String template = "[Document: %s, server: %d, diffs: %s, editor name: %s]";
        return String.format(template, documentName, serverVersion, diffs, editorName);
    }
}
