package github.jk1.editor.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents MobWrite server response, generated as an answer to
 * client diff request. It serves two main purposes: acknowledge
 * client changes reception and postback of all the diffs server
 * have for ths particular client.
 *
 * @author Evgeny Naumenko
 * @see <a href="https://code.google.com/p/google-mobwrite/wiki/Protocol">MobWrite protocol reference</a>
 */
public class MobWriteResponse extends MobWriteMessage {

    private static final String RESPONSE_HEADER = "f:%d:%s\n";
    private static final String CURSOR_LINE = "c:%d\n";

    private List<Integer> responseCursors = new ArrayList<>();

    public MobWriteResponse(String documentName, String token, int version) {
        this.documentName = documentName;
        this.token = token;
        this.version = version;
    }

    public void addDiff(Diff diff) {
        diffs.add(diff);
    }

    public void setResponseCursors(List<Integer> responseCursors) {
        this.responseCursors = responseCursors;
    }

    /**
     * @return string representation of the server's MobWrite response to be sent back to the client
     */
    public String asString() {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format(RESPONSE_HEADER, version, documentName));
        for (Diff element : diffs) {
            builder.append(element);
        }
        Collections.sort(responseCursors);
        for (Integer cursor : responseCursors) {
            builder.append(String.format(CURSOR_LINE, cursor));
        }
        return builder.append('\n').toString();
    }
}
