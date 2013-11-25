package github.jk1.editor.service;

import github.jk1.editor.dao.DocumentDao;
import github.jk1.editor.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

/**
 * @author Evgeny Naumenko
 */
@Service
public class DocumentService {

    private static final Logger LOGGER = Logger.getLogger(DocumentService.class.getName());

    @Autowired
    private ClientChannelService clientChannelService;
    @Autowired
    private DocumentDao dao;

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
        for (DocumentHeader header : dao.getAllDocumentHeaders()) {
            dao.getDocument(header.getId()).deleteView(token);
        }
    }

    /**
     * @param document
     * @param request
     * @return
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
