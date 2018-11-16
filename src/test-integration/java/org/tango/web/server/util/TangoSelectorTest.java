package org.tango.web.server.util;

import com.google.common.collect.Lists;
import org.junit.Test;
import org.tango.rest.entities.Attribute;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11/16/18
 */
public class TangoSelectorTest {

    @Test
    public void selectTangoHosts() {
    }

    @Test
    public void selectDevices() {
    }

    @Test
    public void selectAttributes() {
        TangoSelector instance = new TangoSelector(Lists.newArrayList(new Wildcard("localhost:10000","sys","tg_test","1","double_scalar")));

        List<Attribute> result = instance.selectAttributes();

        assertFalse(result.isEmpty());
    }
}