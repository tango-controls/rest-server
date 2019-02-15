/*
 * Copyright 2019 Tango Controls
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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