package github.jk1.editor.model;

/**
 * An atomic update operation, issued by the client or the server
 *
 * @author Evgeny Naumenko
 */
public class Diff {

    private int version;
    private Mode mode;
    private String payload;

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
        return String.format(mode.getTemplate(), version, payload);
    }

    /**
     * Diff string types:
     * <p/>
     * <ol>
     * <li>Delta - diff between previous text version and a new one</li>
     * <li>Raw - entire text content, for use cases when delta is not informative enough</li>
     * </ol>
     *
     * @author Evgeny Naumenko
     */
    public static enum Mode {

        DELTA("d:%d:%s\n"),
        RAW("R:%d:%s\n");

        private String template;

        private Mode(String template) {
            this.template = template;
        }

        public String getTemplate() {
            return template;
        }
    }
}
