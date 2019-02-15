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

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.Database;

import java.util.List;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11/19/18
 */
public interface TangoDatabaseProxy {
    Database asEsrfDatabase();

    org.tango.client.database.Database asSoleilDatabase();

    String getTangoHost();

    String getFullTangoHost();

    String getHost();

    String getPort();

    String[] getInfo() throws DevFailed;

    List<String> getDeviceNames(String wildcard);

    List<String> getDeviceAttributeNames(String device, String wildcard);

    List<String> getDeviceCommandNames(String device, String wildcard);

    List<String> getDevicePipeNames(String device, String wildcard);

    String getName();

    String getDeviceAlias(String device);
}
