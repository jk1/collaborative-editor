package github.jk1.editor.model;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Evgeny Naumenko
 */
public class MobWriteRequestTest {

    private static final String TEST_CLIENT_MESSAGE = "u:fraser\n" +
            "F:34:abcdef\n" +
            "d:41:=200 -7 +Hello =100\n\n";

    @Test
    public void testFromStream() throws Exception {
        byte[] request = TEST_CLIENT_MESSAGE.getBytes(Charset.forName("UTF-8"));
        MobWriteRequest message = new MobWriteRequest(new ByteArrayInputStream(request));

        assertEquals("fraser", message.getToken());
        assertEquals("abcdef", message.getDocumentName());
        assertEquals(34, message.getVersion());
        List<Diff> diffs = message.getDiffs();
        assertEquals(1, diffs.size());
        Diff diff = diffs.get(0);
        assertEquals(Diff.Mode.DELTA, diff.getMode());
        assertEquals(41, diff.getVersion());
        assertEquals("=200 -7  Hello =100", diff.getPayload());
    }


}
