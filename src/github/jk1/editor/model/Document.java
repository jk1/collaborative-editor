package github.jk1.editor.model;

import name.fraser.neil.plaintext.diff_match_patch;

import java.util.HashMap;
import java.util.Map;

/**
 * <p/>
 * Document instances are thread-safe.
 *
 * @author Evgeny Naumenko
 */
public class Document {

    private diff_match_patch diffMatchPatch;

    //todo: destroy stale views (session listener?)
    private Map<String, View> views = new HashMap<>();

    private final DocumentHeader header;
    private volatile String text = "";

    public Document(diff_match_patch diffMatchPatch, DocumentHeader header) {
        this.diffMatchPatch = diffMatchPatch;
        this.header = header;
    }

    /**
     * Returns a View object for the given unique editor instance name.
     * If no view can be found, a new one is created.
     *
     * @param editorName
     * @return
     */
    public synchronized View getView(String editorName) {
        View view = views.get(editorName);
        if (view == null) {
            view = new View(diffMatchPatch, this, editorName);
            views.put(editorName, view);
        }
        return view;
    }

    public int getId() {
        return header.getId();
    }

    public String getTitle() {
        return header.getTitle();
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

}
