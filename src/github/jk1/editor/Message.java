package github.jk1.editor;

/**
 *
 */
public class Message {

    public static enum Mode{DELTA, RAW}

    private String documentName;
    private String userName;
    private Mode mode;
    private String payload;
    private int serverVersion;
    private int clientVersion;

    public String getDocumentName() {
        return documentName;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public int getServerVersion() {
        return serverVersion;
    }

    public void setServerVersion(int serverVersion) {
        this.serverVersion = serverVersion;
    }

    public int getClientVersion() {
        return clientVersion;
    }

    public void setClientVersion(int clientVersion) {
        this.clientVersion = clientVersion;
    }

    @Override
    public String toString() {
        String template = "[Username: %s, Document: %s ,Mode: %s, client: %d, server: %d, payload: %s]";
        return String.format(template, userName, documentName, mode, clientVersion, serverVersion, payload);
    }
}
