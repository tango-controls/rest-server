package hzg.wpn.mtango;

import org.junit.Test;
import org.tango.web.server.DatabaseDs;

import static junit.framework.Assert.assertEquals;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 23.05.14
 */
public class DatabaseDsTest {
    private static final String TANGO_HOST;

    static {
        TANGO_HOST = System.getenv("TANGO_HOST");
    }

    @Test
    public void testGetDeviceAddress() throws Exception {
        DatabaseDs instance = new DatabaseDs(TANGO_HOST);

        String result = instance.getDeviceAddress("sys/tg_test/1");

        //assume that TangoTest is running on the same host as DB which normally the case
        assertEquals("tango://" + TANGO_HOST + "/sys/tg_test/1", result);
    }
}
