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