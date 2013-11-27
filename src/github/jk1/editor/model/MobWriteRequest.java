package github.jk1.editor.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents client sync request to the server.
 * This request may contain diff information regarding client changes or
 * just a sync request.
 *
 * @author Evgeny Naumenko
 * @see <a href="https://code.google.com/p/google-mobwrite/wiki/Protocol">MobWrite protocol reference</a>
 */
public class MobWriteRequest extends MobWriteMessage {

    private int cursorPosition;
    private boolean deltaEmpty = true;

    private static Set<LineHandler> protocolLineHandlers = new HashSet<LineHandler>() {{
        add(new TokenLineHandler());
        add(new DocumentLineHandler());
        add(new CursorLineHandler());
        add(new DiffLineHandler());
        add(new RawLineHandler());
    }};

    /**
     * @param stream raw client request
     */
    public MobWriteRequest(InputStream stream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            line = URLDecoder.decode(line, "UTF-8");
            // mobWrite explicitly uses \n as a line delimiter regardless of browser or platform
            for (String command : line.split("\n")) {
                this.processCommand(command);
            }
        }
    }

    private void processCommand(String command) {
        if (!command.isEmpty()) {
            for (LineHandler handler : protocolLineHandlers) {
                if (handler.canHandle(command)) {
                    handler.handle(command, this);
                }
            }
        }
    }

    public void setDeltaEmpty(boolean deltaEmpty) {
        this.deltaEmpty = deltaEmpty;
    }

    public boolean isDeltaEmpty() {
        return deltaEmpty;
    }

    public int getCursorPosition() {
        return cursorPosition;
    }

    /**
     * Tiny line parser abstraction. We can add more commands
     * to the protocol without an impact on the existing code
     */
    private static interface LineHandler {

        boolean canHandle(String line);

        void handle(String line, MobWriteRequest request);
    }

    private static class TokenLineHandler implements LineHandler {
        @Override
        public boolean canHandle(String line) {
            return line.toLowerCase().startsWith("u");
        }

        @Override
        public void handle(String line, MobWriteRequest request) {
            request.token = line.substring(2);
        }
    }

    private static class DocumentLineHandler implements LineHandler {
        @Override
        public boolean canHandle(String line) {
            return line.toLowerCase().startsWith("f");
        }

        @Override
        public void handle(String line, MobWriteRequest request) {
            String[] tokens = line.split(":");
            request.version = Integer.parseInt(tokens[1]);
            request.documentName = tokens[2];
        }
    }

    private static class DiffLineHandler implements LineHandler {
        @Override
        public boolean canHandle(String line) {
            return line.toLowerCase().startsWith("d");
        }

        @Override
        public void handle(String line, MobWriteRequest request) {
            line = line.substring(2);
            int delimiter = line.indexOf(":");
            int clientVersion = Integer.parseInt(line.substring(0, delimiter));
            String payload = line.substring(delimiter + 1);
            request.diffs.add(new Diff(Diff.Mode.DELTA, clientVersion, payload));
        }
    }

    private static class RawLineHandler implements LineHandler {
        @Override
        public boolean canHandle(String line) {
            return line.toLowerCase().startsWith("r");
        }

        @Override
        public void handle(String line, MobWriteRequest request) {
            line = line.substring(2);
            int delimiter = line.indexOf(":");
            int clientVersion = Integer.parseInt(line.substring(0, delimiter));
            String payload = line.substring(delimiter + 1);
            request.diffs.add(new Diff(Diff.Mode.RAW, clientVersion, payload));
        }
    }

    private static class CursorLineHandler implements LineHandler {
        @Override
        public boolean canHandle(String line) {
            return line.toLowerCase().startsWith("c");
        }

        @Override
        public void handle(String line, MobWriteRequest request) {
            String position = line.split(":")[2];
            request.cursorPosition = Integer.parseInt(position);
        }
    }
}
