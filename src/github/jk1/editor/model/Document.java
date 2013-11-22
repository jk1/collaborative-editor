package github.jk1.editor.model;

import name.fraser.neil.plaintext.diff_match_patch;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p/>
 * Document instances are thread-safe.
 *
 * @author Evgeny Naumenko
 */
public class Document {

    private final diff_match_patch diffMatchPatch;

    private Map<String, View> views = new ConcurrentHashMap<>();

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
     * @param token
     * @return
     */
    public synchronized View getView(String token) {
        View view = views.get(token);
        if (view == null) {
            view = new View(diffMatchPatch, this, token);
            views.put(token, view);
        }
        return view;
    }

    public Collection<String> getSubscribers(){
       return new HashSet<>(views.keySet()); // defensive copy
    }

    public void deleteView(String token){
        views.remove(token);
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
