package github.jk1.editor.model;

/**
 * An atomic update operation, issued by the client or the server
 *
 * @author Evgeny Naumenko
 */
public class Diff {

    public static enum Mode {DELTA, RAW}

    private int version;
    private Mode mode;
    private String payload;

    public Diff(char mode, int version, String payload) {
        this('d' == mode ? Mode.DELTA : Mode.RAW, version, payload);
    }

    public Diff(Mode mode, int version, String payload) {
        this.version = version;
        this.mode = mode;
        this.payload = payload;
    }

    public int getVersion() {
        return version;
    }

    public Mode getMode() {
        return mode;
    }

    public String getPayload() {
        return payload;
    }

    @Override
    public String toString() {
        String template = mode == Mode.DELTA ? "d:%d:%s\n" : "R:%d:%s\n";
        return String.format(template, version, payload);
    }
}
