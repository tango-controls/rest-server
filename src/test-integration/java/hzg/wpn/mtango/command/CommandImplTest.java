package hzg.wpn.mtango.command;

import org.junit.Test;
import wpn.hdri.tango.proxy.TangoProxyWrapper;

import java.lang.reflect.Method;

import static junit.framework.Assert.assertEquals;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 15.10.12
 */
//TODO make these tests environment independent
public class CommandImplTest {
    @Test
    public void testExecute_Write() throws Exception {
        TangoProxyWrapper proxy = new TangoProxyWrapper("tango://hzgharwi3:10000/sys/tg_test/1");
        Method mtd = proxy.getClass().getMethod("writeAttribute", String.class, Object.class);
        CommandImpl cmd = new CommandImpl(proxy, mtd, "double_scalar_w", Math.PI);

        cmd.execute();
    }

    @Test
    public void testExecute_Read() throws Exception {
        TangoProxyWrapper proxy = new TangoProxyWrapper("tango://hzgharwi3:10000/sys/tg_test/1");
        Method mtd = proxy.getClass().getMethod("readAttributeValueAndTime", String.class);
        CommandImpl cmd = new CommandImpl(proxy, mtd, "double_scalar_w");

        assertEquals(Math.PI, cmd.execute());
    }

    @Test
    public void testExecute_Exec() throws Exception {
        TangoProxyWrapper proxy = new TangoProxyWrapper("tango://hzgharwi3:10000/sys/tg_test/1");
        Method mtd = proxy.getClass().getMethod("executeCommand", String.class, Object.class);
        CommandImpl cmd = new CommandImpl(proxy, mtd, "DevDouble", Math.E);

        assertEquals(Math.E, cmd.execute());
    }
}
