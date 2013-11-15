package github.jk1.editor;

/**
 *
 */
public class View {

    private String userName;
    private Document document;
    private String shadow;
    private String backupShadow;
    private int shadowClientVersion;
    private int shadowServerVersion;
    private int backUpShadowServerVersion;
    private boolean deltaOk;


    public View(String userName, Document document) {
        this.userName = userName;
        this.document = document;
    }
}
