package org.tango.web.server.util;

import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11/16/18
 */
public class WildcardExtractorTest {

    @Test
    public void testExtractWildcards() {
        WildcardExtractor instance = new WildcardExtractor();

        List<Wildcard> result = instance.extractWildcards(Lists.newArrayList("localhost:10000/sys/tg_test/1"));

        assertFalse(result.isEmpty());

        assertEquals("localhost:10000", result.get(0).host);
        assertEquals("sys", result.get(0).domain);
        assertEquals("tg_test", result.get(0).family);
        assertEquals("1", result.get(0).member);
        assertNull(result.get(0).attribute);
    }

    @Test
    public void testExtractWildcards_WrongWildcard() {
        WildcardExtractor instance = new WildcardExtractor();

        List<Wildcard> result = instance.extractWildcards(Lists.newArrayList("xyz"));

        //TODO implement validation
        assertFalse(result.isEmpty());
        assertEquals("xyz", result.get(0).host);
    }
}