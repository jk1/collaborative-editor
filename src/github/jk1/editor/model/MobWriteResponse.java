package github.jk1.editor.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Evgeny Naumenko
 */
public class MobWriteResponse extends MobWriteMessage {

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
}
