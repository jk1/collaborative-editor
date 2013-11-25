package github.jk1.editor.model;

import java.util.ArrayList;

import java.util.List;

/**
 * Represent diff-posting request from a client or a server response.
 * This class is able to perform serialization routine from Java objects
 * to MobWrite protocol messages and vise versa.
 *
 * @author Evgeny Naumenko
 * @see <a href="https://code.google.com/p/google-mobwrite/wiki/Protocol">MobWrite protocol reference</a>
 */
public abstract class MobWriteMessage {

    protected String documentName;
    protected String token;
    protected int version;

    protected List<Diff> diffs = new ArrayList<>();

    protected MobWriteMessage() {
    }

    /**
     * @return
     */
    public String getDocumentName() {
        return documentName;
    }

    /**
     * @return unique token of editor instance, which generates the request, same as channel token
     */
    public String getToken() {
        return token;
    }

    /**
     * @return
     */
    public int getVersion() {
        return version;
    }

    public List<Diff> getDiffs() {
        return diffs;
    }

    @Override
    public String toString() {
        String template = "[Document: %s, server: %d, diffs: %s, editor name: %s]";
        return String.format(template, documentName, version, diffs, token);
    }
}
