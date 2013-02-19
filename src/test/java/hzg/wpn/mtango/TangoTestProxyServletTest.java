package hzg.wpn.mtango;

import fr.esrf.Tango.DevError;
import fr.esrf.Tango.DevFailed;
import hzg.wpn.mtango.command.CommandFactory;
import org.junit.Test;
import wpn.hdri.tango.proxy.TangoProxyException;
import wpn.hdri.tango.proxy.TangoProxyWrapper;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import java.lang.reflect.Method;

import static org.mockito.Mockito.*;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 10.10.12
 */
public class TangoTestProxyServletTest {
    @Test(expected = ServletException.class)
    public void testInit_Failed() throws Exception {
        TangoProxyWrapperFactory mockFactory = mock(TangoProxyWrapperFactory.class);
        doThrow(new TangoProxyException(new DevFailed("blah", new DevError[]{}))).when(mockFactory).createTangoProxyWrapper(anyString());
        TangoProxyServlet instance = new TangoProxyServlet(mockFactory, new CommandFactory());

        ServletConfig mockConfig = mock(ServletConfig.class);
        doReturn("hzgharwi3:10000").when(mockConfig).getInitParameter("tango-host");
        doReturn("sys/tg_test/1").when(mockConfig).getInitParameter("tango-device");

        instance.init(mockConfig);
    }
}
