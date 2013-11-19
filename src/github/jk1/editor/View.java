package github.jk1.editor;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * Represents a document view for a particular client editor instance.
 * A single user may open several editors in scope of the one web session,
 * and all his editors are treated independently, as though the have been
 * created by different users.
 *
 * View uses a "shadow" as a representation of what server thinks client text is.
 * "Backup shadow" is effectively a "shadow" one version back, to roll the shadow back
 * in case of a temporary network failure.
 *
 * @author Evgeny Naumenko
 */
public class View {

    private Document document;
    private String editorInstanceName;
    private String shadow;
    private String backupShadow;
    private int shadowClientVersion = 0;
    private int shadowServerVersion = 0;
    private int backUpShadowServerVersion = 0;
    private boolean deltaOk;

    /**
     *
     */
    private LinkedList<Diff> editStack = new LinkedList<>();

    public View(Document document, String editorInstanceName) {
        this.document = document;
        this.editorInstanceName = editorInstanceName;
        this.shadow = document.getText();
        this.shadow = document.getTitle();
    }

    public void rollback() {
        this.shadow = backupShadow;
        this.shadowServerVersion = this.getBackUpShadowServerVersion();
        editStack.clear();
    }

    public void dropObsoleteEditStackRecords(int currentVersion) {
        Iterator<Diff> iterator = editStack.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getVersion() < currentVersion) {
                iterator.remove();
            }
        }
    }

    public void incrementShadowServerVersion() {
        shadowServerVersion++;
    }

    public void incrementShadowClientVersion() {
        shadowClientVersion++;
    }

    public Document getDocument() {
        return document;
    }

    public String getShadow() {
        return shadow;
    }

    public void setShadow(String shadow) {
        this.shadow = shadow;
    }

    public String getBackupShadow() {
        return backupShadow;
    }

    public String getEditorInstanceName() {
        return editorInstanceName;
    }

    public void setBackupShadow(String backupShadow) {
        this.backupShadow = backupShadow;
    }

    public int getShadowClientVersion() {
        return shadowClientVersion;
    }

    public void setShadowClientVersion(int shadowClientVersion) {
        this.shadowClientVersion = shadowClientVersion;
    }

    public int getShadowServerVersion() {
        return shadowServerVersion;
    }

    public void setShadowServerVersion(int shadowServerVersion) {
        this.shadowServerVersion = shadowServerVersion;
    }

    public int getBackUpShadowServerVersion() {
        return backUpShadowServerVersion;
    }

    public void setBackUpShadowServerVersion(int backUpShadowServerVersion) {
        this.backUpShadowServerVersion = backUpShadowServerVersion;
    }

    public boolean isDeltaOk() {
        return deltaOk;
    }

    public void setDeltaOk(boolean deltaOk) {
        this.deltaOk = deltaOk;
    }

    public LinkedList<Diff> getEditStack() {
        return editStack;
    }
}
