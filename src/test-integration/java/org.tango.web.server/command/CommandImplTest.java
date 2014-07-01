package org.tango.web.server.command;

import org.junit.Test;
import org.tango.client.ez.proxy.TangoProxies;
import org.tango.client.ez.proxy.TangoProxy;

import java.lang.reflect.Method;

import static junit.framework.Assert.assertEquals;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 15.10.12
 */
//TODO make these tests environment independent
public class CommandImplTest {
    private final String tangoHost = System.getenv("TANGO_HOST");

    @Test
    public void testExecute_WriteRead() throws Exception {
        TangoProxy proxy = TangoProxies.newDeviceProxyWrapper("tango://" + tangoHost + "/sys/tg_test/1");
        Method mtd = proxy.getClass().getMethod("writeAttribute", String.class, Object.class);
        Command cmd = new Command(proxy, mtd, "double_scalar_w", Math.PI);

        cmd.execute();

        mtd = proxy.getClass().getMethod("readAttributeValueAndTime", String.class);
        cmd = new Command(proxy, mtd, "double_scalar_w");

        assertEquals(Math.PI, cmd.execute());
    }

    @Test
    public void testExecute_Exec() throws Exception {
        TangoProxy proxy = TangoProxies.newDeviceProxyWrapper("tango://" + tangoHost + "/sys/tg_test/1");
        Method mtd = proxy.getClass().getMethod("executeCommand", String.class, Object.class);
        Command cmd = new Command(proxy, mtd, "DevDouble", Math.E);

        assertEquals(Math.E, cmd.execute());
    }
}
