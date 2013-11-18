package github.jk1.editor;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class Document {

    private transient Map<String, View> views = new HashMap<>();

    private Header header;
    private String text;

    public Document(Header header) {
        this(header, "");
    }

    public Document(Header header, String text) {
        this.header = header;
        this.text = text;
    }

    public synchronized View getView(String userName) {
        View view = views.get(userName);
        if (view == null) {
            view = new View(userName, this);
            views.put(userName, view);
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
    }
}
