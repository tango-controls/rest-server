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

import org.tango.client.ez.proxy.TangoProxy;

import java.util.List;

/**
 * @author ingvord
 * @since 11/18/18
 */
public interface TangoDeviceProxy {
    String getName();

    String getAlias();

    String getFullName();

    String getHost();

    TangoProxy getProxy();

    List<String> getAttributeNames(String wildcard);

    List<String> getCommandNames(String wildcard);

    List<String> getPipeNames(String wildcard);
}
