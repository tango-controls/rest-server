package org.tango.web.server.util;

import com.google.common.collect.Lists;
import fr.soleil.tango.clientapi.TangoAttribute;
import org.junit.Test;
import org.tango.web.server.TangoProxyPool;
import org.tango.web.server.proxy.TangoAttributeProxy;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11/16/18
 */
public class TangoSelectorTest {
    private TangoProxyPool proxyPool = mock(TangoProxyPool.class);


    @Test
    public void selectTangoHosts() {
    }

    @Test
    public void selectDevices() {
    }

    @Test
    public void selectAttributes() {
        TangoSelector instance = new TangoSelector(Lists.newArrayList(new Wildcard("localhost:10000","sys","tg_test","1","double_scalar")), proxyPool);

        List<TangoAttributeProxy> result = instance.selectAttributesStream().collect(Collectors.toList());

        assertFalse(result.isEmpty());
    }
}