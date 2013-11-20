package github.jk1.editor;

import org.codehaus.jackson.map.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

/**
 * <p/>
 * Document instances are thread-safe.
 *
 * @author Evgeny Naumenko
 */
public class Document {

    //todo: destroy stale views (session listener?)
    private Map<String, View> views = new HashMap<>();

    private final Header header;
    private volatile String text = "";

    public Document(Header header) {
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
            view = new View(this, editorName);
            views.put(editorName, view);
        }
        return view;
    }

    public int getId() {
        return header.getId();
    }

    public String getTitle() {
        return header.getName();
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    /**
     * Document header is used for document identification. Header
     * does not contain the full document text, so it's ok to
     * send headers list over the wire without any pagination involved
     */
    public static class Header {
        private final Integer id;
        private final String name;

        public Header(Integer id) {
            this(id, "");
        }

        public Header(Integer id, String name) {
            this.id = id;
            this.name = name;
        }

        public Integer getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        @Override
        public boolean equals(Object o) {
            return o != null
                    && getClass() == o.getClass()
                    && id.equals(((Header) o).id);
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }

        @Override
        public String toString() {
            return "[id=" + id + ", name=" + name + "]";
        }
    }
}
