package github.jk1.editor.model;

import name.fraser.neil.plaintext.StandardBreakScorer;
import name.fraser.neil.plaintext.diff_match_patch;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.junit.Assert.*;

/**
 * @author Evgeny Naumenko
 */
@RunWith(MockitoJUnitRunner.class)
public class ViewTest {

    private diff_match_patch dmp = new diff_match_patch(new StandardBreakScorer());
    private static final String DOCUMENT_TEXT = "Document text";

    @Mock
    private MobWriteRequest request;

    private View view;


    @Before
    public void setUp() {
        Document document = new Document(dmp, new DocumentHeader(1));
        document.setText(DOCUMENT_TEXT);
        view = document.getView("token");
        when(request.getDocumentName()).thenReturn("name");
    }

    @Test
    public void testRequestRaw() {
        when(request.getVersion()).thenReturn(0);
        List<Diff> diffs = new ArrayList<>();
        diffs.add(new Diff(Diff.Mode.RAW, 0, ""));
        when(request.getDiffs()).thenReturn(diffs);

        MobWriteResponse response = view.apply(request);

        assertEquals(request.getDocumentName(), response.getDocumentName());
        assertEquals(1, response.getDiffs().size());
        Diff diff = response.getDiffs().get(0);
        assertEquals(Diff.Mode.DELTA, diff.getMode());
        assertEquals("+" + DOCUMENT_TEXT, diff.getPayload());
    }

    @Test
    public void testApplyDelta() {
        testRequestRaw();
        reset(request);
        when(request.getVersion()).thenReturn(1);
        List<Diff> diffs = new ArrayList<>();
        diffs.add(new Diff(Diff.Mode.DELTA, 0, "=9\t-4\t+diff"));
        when(request.getDiffs()).thenReturn(diffs);

        MobWriteResponse response = view.apply(request);

        assertEquals(request.getDocumentName(), response.getDocumentName());
        assertEquals(1, response.getDiffs().size());
        Diff diff = response.getDiffs().get(0);
        assertEquals(Diff.Mode.DELTA, diff.getMode());
        assertEquals("=13", diff.getPayload());
    }

    @Test
    public void testCorruptedDelta() {
        testRequestRaw();
        reset(request);
        when(request.getVersion()).thenReturn(1);
        List<Diff> diffs = new ArrayList<>();
        diffs.add(new Diff(Diff.Mode.DELTA, 0, "=100"));
        when(request.getDiffs()).thenReturn(diffs);

        MobWriteResponse response = view.apply(request);

        assertEquals(request.getDocumentName(), response.getDocumentName());
        assertEquals(1, response.getDiffs().size());
        Diff diff = response.getDiffs().get(0);
        assertEquals(Diff.Mode.RAW, diff.getMode());
        assertEquals(DOCUMENT_TEXT, diff.getPayload());
    }

    @Test
    public void testRestoreShadowFromBackup() {
        testRequestRaw();
        reset(request);
        when(request.getVersion()).thenReturn(0);
        List<Diff> diffs = new ArrayList<>();
        diffs.add(new Diff(Diff.Mode.DELTA, 0, "=9\t-4\t+diff"));
        when(request.getDiffs()).thenReturn(diffs);

        MobWriteResponse response = view.apply(request);

        assertEquals(request.getDocumentName(), response.getDocumentName());
        assertEquals(1, response.getDiffs().size());
        Diff diff = response.getDiffs().get(0);
        assertEquals(Diff.Mode.RAW, diff.getMode());
        assertEquals(DOCUMENT_TEXT, diff.getPayload());
    }
}
