package github.jk1.editor;

import java.util.Iterator;
import java.util.LinkedList;

/**
 *
 */
public class View {

    private String userName;
    private Document document;
    private String shadow;
    private String backupShadow;
    private int shadowClientVersion = 0;
    private int shadowServerVersion = 0;
    private int backUpShadowServerVersion = 0;
    private boolean deltaOk;
    private LinkedList<EditStackElement> editStack = new LinkedList<>();

    public View(String userName, Document document) {
        this.userName = userName;
        this.document = document;
        this.shadow = document.getText();
        this.shadow = document.getTitle();
    }

    public void rollback() {
        this.shadow = backupShadow;
        this.shadowServerVersion = this.getBackUpShadowServerVersion();
        editStack.clear();
    }

    public void dropObsoleteEditStackRecords(int currentVersion) {
        Iterator<EditStackElement> iterator = editStack.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().version < currentVersion) {
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

    public String getUserName() {
        return userName;
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

    public LinkedList<EditStackElement> getEditStack() {
        return editStack;
    }

    public static class EditStackElement {
        public final int version;
        public final String edit;

        public EditStackElement(int version, String edit) {
            this.version = version;
            this.edit = edit;
        }
    }
}
