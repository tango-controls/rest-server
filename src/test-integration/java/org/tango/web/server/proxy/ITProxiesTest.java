package org.tango.web.server.proxy;

import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author ingvord
 * @since 11/25/18
 */
public class ITProxiesTest {

    @Test
    public void optionalTangoDeviceProxy() {
        Optional<TangoDeviceProxy> optionalTangoDeviceProxy = Proxies.optionalTangoDeviceProxy("tango://localhost:10000/sys/tg_test/1");
        assertTrue(optionalTangoDeviceProxy.isPresent());
        assertEquals("localhost:10000", optionalTangoDeviceProxy.get().getHost());
        assertEquals("sys/tg_test/1", optionalTangoDeviceProxy.get().getName());
    }

    @Test
    public void optionalTangoCommandProxy(){
        Optional<TangoCommandProxy> optionalTangoCommandProxy = Proxies.optionalTangoCommandProxy("tango://localhost:10000/sys/tg_test/1/DevString");
        assertTrue(optionalTangoCommandProxy.isPresent());
        assertEquals("DevString", optionalTangoCommandProxy.get().getCommandName());
    }
}