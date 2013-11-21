package github.jk1.editor.dao;

/**
 * Indicates that object requested by the client has not been found
 *
 * @author Evgeny Naumenko
 */
public class NotFoundException extends RuntimeException{

    public NotFoundException(String message) {
        super(message);
    }
}
