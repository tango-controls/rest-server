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
import org.tango.client.ez.proxy.TangoProxy;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author ingvord
 * @since 11/18/18
 */
public class TangoDeviceProxyImpl implements TangoDeviceProxy {
    private final String host;
    private final String name;
    private final TangoProxy proxy;
    private final String fullName;

    public TangoDeviceProxyImpl(String host, String name, TangoProxy proxy) {
        this.fullName = "tango://" + host + "/" + name;
        this.host = host;
        this.name = name;
        this.proxy = proxy;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getAlias() {
        try {
            return proxy.toDeviceProxy().get_alias();
        } catch (DevFailed devFailed) {
            return null;
        }
    }

    @Override
    public String getFullName() {
        return fullName;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public TangoProxy getProxy() {
        return proxy;
    }

    @Override
    public List<String> getAttributeNames(String wildcard) {
        try {
            return Arrays.stream(proxy.toDeviceProxy().get_attribute_list())
                    .filter(s -> wildcard.equals("*") || s.equalsIgnoreCase(wildcard))
                    .collect(Collectors.toList());
        } catch (DevFailed devFailed) {
            return Collections.emptyList();
        }
    }

    @Override
    public List<String> getCommandNames(String wildcard) {
        try {
            return Arrays.stream(proxy.toDeviceProxy().command_list_query())
                    .map(commandInfo -> commandInfo.cmd_name)
                    .filter(s -> wildcard.equals("*") || s.equalsIgnoreCase(wildcard))
                    .collect(Collectors.toList());
        } catch (DevFailed devFailed) {
            return Collections.emptyList();
        }
    }

    @Override
    public List<String> getPipeNames(String wildcard) {
        try {
            return proxy.toDeviceProxy().getPipeNames().stream()
                    .filter(s -> wildcard.equals("*") || s.equalsIgnoreCase(wildcard))
                    .collect(Collectors.toList());
        } catch (DevFailed devFailed) {
            return Collections.emptyList();
        }
    }
}
