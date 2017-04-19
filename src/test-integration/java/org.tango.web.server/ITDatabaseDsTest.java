package org.tango.web.server;

import org.junit.Test;
import org.tango.client.ez.proxy.TangoProxies;
import org.tango.client.ez.proxy.TangoProxy;

import java.util.Collection;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 23.05.14
 */
public class ITDatabaseDsTest {
    private static final String TANGO_HOST;

    static {
        TANGO_HOST = System.getenv("TANGO_HOST");
    }

    @Test
    public void testGetDeviceAddress() throws Exception {
        TangoProxy proxy = TangoProxies.newDeviceProxyWrapper("tango://" + TANGO_HOST + "/" + TangoContext.SYS_DATABASE_2);
        DatabaseDs instance = new DatabaseDs(proxy);

        String result = instance.getDeviceAddress("sys/tg_test/1");

        //assume that TangoTest is running on the same host as DB which normally the case
        assertEquals("tango://" + TANGO_HOST + "/sys/tg_test/1", result);
    }

    @Test
    public void testGetDeviceList() throws Exception {
        TangoProxy proxy = TangoProxies.newDeviceProxyWrapper("tango://" + TANGO_HOST + "/" + TangoContext.SYS_DATABASE_2);
        DatabaseDs instance = new DatabaseDs(proxy);

        Collection<String> result = instance.getDeviceList();

        //assume that TangoTest is running on the same host as DB which normally the case
        assertTrue(result.contains("sys/tg_test/1"));
    }
}
