package org.tango.web.server;


import org.junit.Before;
import org.junit.Test;
import org.tango.client.ez.proxy.TangoProxies;
import org.tango.client.ez.proxy.TangoProxy;
import org.tango.rest.test.Rc4Test;

import static org.junit.Assert.assertTrue;

public class ITAccessControlTest {
    private final String tangoHost = System.getenv("TANGO_HOST");

    @Test
    public void testCheckUserCanRead() throws Exception {
        TangoProxy proxy = TangoProxies.newDeviceProxyWrapper("tango://" + tangoHost + "/sys/access_control/1");
        AccessControl instance = new AccessControl(proxy);

        assertTrue(instance.checkUserCanRead("test", "127.0.0.1", "sys/tg_test/1"));
    }

}