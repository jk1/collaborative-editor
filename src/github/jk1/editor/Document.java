package github.jk1.editor;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class Document {
    private Map<String, View> views = new HashMap<>();
    private String title;
    private String text;

    public synchronized View getView(String userName) {
        View view = views.get(userName);
        if (view == null) {
            view = new View(userName, this);
            views.put(userName, view);
        }
        return view;
    }
}
