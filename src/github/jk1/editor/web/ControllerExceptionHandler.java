package github.jk1.editor.web;

import github.jk1.editor.dao.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Dedicated applications exception handler.
 * Some exceptions are supposed to be thrown out of
 * controllers and be handled here.
 *
 * @author Evgeny Naumenko
 */
@ControllerAdvice
public class ControllerExceptionHandler {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    public void handle404() {
        // declaration above does all the magic
    }
}
