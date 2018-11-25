package org.tango.web.server;


import org.junit.Test;
import org.tango.client.ez.proxy.TangoProxies;
import org.tango.client.ez.proxy.TangoProxy;

import static org.junit.Assert.assertTrue;

public class ITAccessControlTest {
    @Test
    public void testCheckUserCanRead() throws Exception {
        TangoProxy proxy = TangoProxies.newDeviceProxyWrapper("tango://localhost:10000/sys/access_control/1");
        AccessControl instance = new AccessControl(proxy);

        assertTrue(instance.checkUserCanRead("test", "127.0.0.1", "sys/tg_test/1"));
    }

}