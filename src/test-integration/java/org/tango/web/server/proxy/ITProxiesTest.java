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