package github.jk1.editor.model;

/**
 * Document header is used for document identification. Header
 * does not contain the full document text, so it's ok to
 * send headers list over the wire without any pagination involved.
 * <p/>
 * Document header is a mere wrapper around document id, title may be
 * omitted
 *
 * @author Evvgeny Naumenko
 */
public class DocumentHeader {
    private final int id;
    private final String title;

    public DocumentHeader(Integer id) {
        this(id, null);
    }

    public DocumentHeader(int id, String title) {
        this.id = id;
        this.title = title;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public boolean equals(Object o) {
        return o != null
                && getClass() == o.getClass()
                && id == (((DocumentHeader) o).id);
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return "[id=" + id + ", title=" + title + "]";
    }
}
