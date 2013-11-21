package github.jk1.editor.model;

/**
 * An atomic update operation, issued by the client or the server
 *
 * @author Evgeny Naumenko
 */
public class Diff {

    private int version;
    private DiffMode mode;
    private String payload;

    public Diff(char mode, int version, String payload) {
        this('d' == mode ? DiffMode.DELTA : DiffMode.RAW, version, payload);
    }

    public Diff(DiffMode mode, int version, String payload) {
        this.version = version;
        this.mode = mode;
        this.payload = payload;
    }

    public int getVersion() {
        return version;
    }

    public DiffMode getMode() {
        return mode;
    }

    public String getPayload() {
        return payload;
    }

    @Override
    public String toString() {
        String template = mode == DiffMode.DELTA ? "d:%d:%s\n" : "R:%d:%s\n";
        return String.format(template, version, payload);
    }
}
