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

package org.tango.web.server.response;

import fr.esrf.TangoApi.DeviceInfo;
import org.tango.rest.v10.entities.Device;

import java.net.URI;

/**
 * @author ingvord
 * @since 11/18/18
 */
public class TangoRestDevice extends Device {
    public DeviceInfo info;

    public TangoRestDevice(String id, String name, String host, String alias, DeviceInfo info, URI href) {
        super(id, name, host, alias, null, href);
        this.info = info;
    }
}
