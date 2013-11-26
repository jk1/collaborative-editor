package github.jk1.editor.service;

import github.jk1.editor.dao.DocumentDao;
import github.jk1.editor.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

/**
 * Performs common operations on documents or groups of documents
 *
 * @author Evgeny Naumenko
 */
@Service
public class DocumentService {

    private static final Logger LOGGER = Logger.getLogger(DocumentService.class.getName());

    private ClientChannelService clientChannelService;
    private DocumentDao dao;

    @Autowired
    public DocumentService(ClientChannelService clientChannelService, DocumentDao dao) {
        this.clientChannelService = clientChannelService;
        this.dao = dao;
    }

    /**
     * Creates a new document with a given name and sends notifications
     * to all available user, that a document has been added.
     *
     * @param name non-unique, non-null name for a new  document
     */
    public void createDocument(String name) {
        dao.createDocument(name);
        clientChannelService.broadcastDocumentListUpdate();
    }

    /**
     * Deletes the view with a given token.
     *
     * @param token unique view and channel identifier
     */
    public void deleteView(String token) {
        LOGGER.info("Removing view " + token);
        for (DocumentHeader header : dao.getAllDocumentHeaders()) {
            dao.getDocument(header.getId()).deleteView(token);
        }
    }

    /**
     * Applies given diff request to the document
     *
     * @return response to be sent back to client
     */
    public MobWriteResponse applyClientMessage(Document document, MobWriteRequest request) {
        View view = document.getView(request.getToken());
        MobWriteResponse response = view.apply(request);
        if (!request.isDeltaEmpty()) {
            clientChannelService.broadcastDocumentUpdate(document, request.getToken());
        }
        return response;
    }
}
