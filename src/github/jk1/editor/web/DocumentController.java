package github.jk1.editor.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 */
@Controller("/document")
public class DocumentController {


    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView getDocument() {
       return new ModelAndView("editor");
    }

    @RequestMapping(method = RequestMethod.POST)
    public void postChanges() {

    }
}
