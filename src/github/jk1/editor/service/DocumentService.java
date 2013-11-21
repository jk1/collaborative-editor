package github.jk1.editor.service;

import github.jk1.editor.dao.DocumentDao;
import github.jk1.editor.model.Document;
import github.jk1.editor.model.MobWriteMessage;
import github.jk1.editor.model.View;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

import static github.jk1.editor.service.ClientNotificationService.MessageKey.UPDATE_DOCUMENT;
import static github.jk1.editor.service.ClientNotificationService.MessageKey.UPDATE_LIST;

/**
 * @author Evgeny Naumenko
 */
@Service
public class DocumentService {

    private static final Logger LOGGER = Logger.getLogger(DocumentService.class.getName());

    private ClientNotificationService clientNotificationService;
    private DocumentDao dao;

    @Autowired
    public DocumentService(ClientNotificationService clientNotificationService, DocumentDao dao) {
        this.clientNotificationService = clientNotificationService;
        this.dao = dao;
    }

    /**
     *
     * @param name
     */
    public void createDocument(String name) {
        dao.createDocument(name);
        clientNotificationService.broadcast(UPDATE_LIST, ClientNotificationService.BROADCAST_TO_ALL);
    }

    /**
     *
     * @param document
     * @param message
     * @return
     */
    public MobWriteMessage applyClientMessage(Document document, final MobWriteMessage message) {
        View view = document.getView(message.getEditorName());
        MobWriteMessage response = view.process(message);
        clientNotificationService.broadcast(UPDATE_DOCUMENT, new ClientNotificationService.BroadcastFilter() { // todo: separate thread for speedup?
            @Override
            public boolean broadcastToChannel(String channelToken) {
                //to all but the diff author
                return !channelToken.equals(message.getEditorName());
            }
        });
        return response;
    }

}
