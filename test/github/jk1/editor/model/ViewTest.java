package github.jk1.editor.model;

import name.fraser.neil.plaintext.StandardBreakScorer;
import name.fraser.neil.plaintext.diff_match_patch;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.junit.Assert.*;

/**
 * @author Evgeny Naumenko
 */
@RunWith(MockitoJUnitRunner.class)
public class ViewTest {

    @Mock
    private diff_match_patch dmp = new diff_match_patch(new StandardBreakScorer());
    @Mock
    private MobWriteRequest request;

    private Document document;
    private View view;


    @Before
    public void setUp() {
        document = new Document(dmp, new DocumentHeader(1));
        view = document.getView("token");
        when(request.getDocumentName()).thenReturn("name");
    }

    @Test
    public void testApplyDelta() {
        when(request.getVersion()).thenReturn(1);
        when(request.getDiffs()).thenReturn(Collections.singletonList(new Diff(Diff.Mode.RAW, 1, "R:0:\n")));

        MobWriteResponse response = view.apply(request);

        assertEquals(response.getDocumentName(), request.getDocumentName());
        assertEquals(1, response.getDiffs().size());
    }

    @Test
    public void testDeltaStack() {

    }

    @Test
    public void testCorruptedDelta() {

    }

    @Test
    public void testRequestRaw() {

    }


    @Test
    public void testCursorPosition() {

    }
}
