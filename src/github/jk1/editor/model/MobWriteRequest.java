package github.jk1.editor.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;

/**
 *
 * todo: introduce separate command handlers
 *
 * @author Evgeny Naumenko
 */
public class MobWriteRequest extends MobWriteMessage {

    private int cursorPosition;
    private boolean deltaEmpty = true;


    /**
     * @param stream raw client request
     * @return parsed client message for further processing
     * @throws java.io.IOException if client request is not parable
     */
    public MobWriteRequest(InputStream stream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            line = URLDecoder.decode(line, "UTF-8");
            // mobWrite explicitly uses \n as a line delimiter regardless of browser or platform
            for (String command : line.split("\n")) {
                if (!command.isEmpty()) {
                    parseCommand(command);
                }
            }
        }
    }

    private void parseCommand(String command) {
        char name = command.substring(0, 1).toLowerCase().charAt(0);
        String value = command.substring(2);
        if ('u' == name) {
            this.token = value;
        } else {
            int div = value.indexOf(':');
            if (div == -1) {
                return; // line seems to be corrupted, skip it
            }
            if ('f' == name) {
                this.version = Integer.parseInt(value.substring(0, div));
                this.documentName = value.substring(div + 1);
            } else if ('c' == name) {
                this.cursorPosition = Integer.parseInt(value.substring(div + 1));
            } else {
                int clientVersion = Integer.parseInt(value.substring(0, div));
                String payload = value.substring(div + 1); // strip colon
                this.diffs.add(new Diff(name, clientVersion, payload));
            }
        }
    }

    public void setDeltaEmpty(boolean deltaEmpty) {
        this.deltaEmpty = deltaEmpty;
    }

    public boolean isDeltaEmpty(){
        return deltaEmpty;
    }

    public int getCursorPosition() {
        return cursorPosition;
    }
}
