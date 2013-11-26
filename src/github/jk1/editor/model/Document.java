package github.jk1.editor.model;

import name.fraser.neil.plaintext.diff_match_patch;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents an editable text unit. Each document maintains a set of views,
 * one view for each active editor. Document instances are thread-safe,
 * although external synchronization is necessary to perform getText()/setText() call pair.
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
     * If no view can be found, a new one is created and returned.
     */
    public synchronized View getView(String token) {
        View view = views.get(token);
        if (view == null) {
            view = new View(diffMatchPatch, this, token);
            views.put(token, view);
        }
        return view;
    }

    /**
     * Returns a list of cursor offset for all active document editors
     * but the current user. Current user is identified by unique user token
     *
     * todo: this logic probably does not belong here
     */
    public List<Integer> getConcurrentCursors(String authorToken) {
        List<Integer> cursors = new ArrayList<>(views.size());
        for (Map.Entry<String, View> entry : views.entrySet()) {
            if (!entry.getKey().equals(authorToken)) {
                cursors.add(entry.getValue().getCursorPosition());
            }
        }
        return cursors;
    }

    /**
     * @return list of users currently working with this document
     */
    public Collection<String> getEditors() {
        return new HashSet<>(views.keySet()); // defensive copy
    }

    public void deleteView(String token) {
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
