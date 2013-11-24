package github.jk1.editor.model;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Evgeny Naumenko
 */
public class MobWriteMessageTest {

    private static final String TEST_CLIENT_MESSAGE = "u:fraser\n" +
            "F:34:abcdef\n" +
            "d:41:=200 -7 +Hello =100\n\n";

    private static final String TEST_SERVER_RESPONSE = "f:42:abcdef\n" +
            "d:34:=305\n\n";

    @Test
    public void testFromStream() throws Exception {
        byte[] request = TEST_CLIENT_MESSAGE.getBytes(Charset.forName("UTF-8"));
        MobWriteMessage message = MobWriteMessage.fromStream(new ByteArrayInputStream(request));

        assertEquals("fraser", message.getToken());
        assertEquals("abcdef", message.getDocumentName());
        assertEquals(34, message.getVersion());
        List<Diff> diffs = message.getDiffs();
        assertEquals(1, diffs.size());
        Diff diff = diffs.get(0);
        assertEquals(DiffMode.DELTA, diff.getMode());
        assertEquals(41, diff.getVersion());
        assertEquals("=200 -7  Hello =100", diff.getPayload());
    }

    @Test
    public void testAsString() throws Exception {
        MobWriteMessage message = new MobWriteMessage("abcdef", "fraser", 42);
        message.addDiff(new Diff(DiffMode.DELTA, 34, "=305"));

        assertEquals(TEST_SERVER_RESPONSE, message.asString());
    }
}
