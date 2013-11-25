package github.jk1.editor.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Evgeny Naumenko
 */
public class MobWriteResponseTest {

    private static final String TEST_SERVER_RESPONSE = "f:42:abcdef\n" +
            "d:34:=305\n\n";

    @Test
    public void testAsString() throws Exception {
        MobWriteResponse message = new MobWriteResponse("abcdef", "fraser", 42);
        message.addDiff(new Diff(DiffMode.DELTA, 34, "=305"));

        assertEquals(TEST_SERVER_RESPONSE, message.asString());
    }
}
