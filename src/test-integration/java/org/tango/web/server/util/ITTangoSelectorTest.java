package org.tango.web.server.util;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.tango.web.server.Context;
import org.tango.web.server.proxy.TangoAttributeProxy;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertFalse;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11/16/18
 */
public class ITTangoSelectorTest {
    private Context context;

    @Before
    public void before() {
        context = new Context();
    }


    @Test
    public void selectAttributes() {
        TangoSelector instance = new TangoSelector(Lists.newArrayList(new Wildcard("localhost:10000", "sys", "tg_test", "1", "double_scalar")), context);

        List<TangoAttributeProxy> result = instance.selectAttributesStream().collect(Collectors.toList());

        assertFalse(result.isEmpty());
    }
}