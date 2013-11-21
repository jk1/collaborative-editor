package github.jk1.editor.model;

/**
 * Document header is used for document identification. Header
 * does not contain the full document text, so it's ok to
 * send headers list over the wire without any pagination involved.
 * <p/>
 * Document header is a mere wrapper around document id, name may be
 * omitted
 *
 * @author Evvgeny Naumenko
 */
public class DocumentHeader {
    private final Integer id;
    private final String name;

    public DocumentHeader(Integer id) {
        this(id, null);
    }

    public DocumentHeader(Integer id, String name) {
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
                && id.equals(((DocumentHeader) o).id);
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
