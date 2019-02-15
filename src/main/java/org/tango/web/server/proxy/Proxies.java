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
import fr.esrf.TangoApi.DeviceProxy;
import fr.esrf.TangoApi.DeviceProxyFactory;
import fr.soleil.tango.clientapi.TangoAttribute;
import fr.soleil.tango.clientapi.TangoCommand;
import org.tango.client.database.DatabaseFactory;
import org.tango.client.ez.proxy.TangoProxies;
import org.tango.client.ez.proxy.TangoProxy;
import org.tango.client.ez.proxy.TangoProxyException;
import org.tango.client.ez.util.TangoUtils;
import org.tango.utils.DevFailedUtils;
import org.tango.web.server.util.TangoDeviceProxyUtils;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.tango.web.server.providers.TangoDatabaseProvider.DEFAULT_TANGO_PORT;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11/19/18
 */
public class Proxies {
    private Proxies(){}

    public static TangoAttributeProxy newTangoAttributeProxy(String fullAttributeName) throws DevFailed {
        Matcher matcher = TANGO_MEMBER_FULL_NAME_PATTERN.matcher(fullAttributeName);
        if(!matcher.matches()) throw new AssertionError();
        TangoAttribute tangoAttribute = new TangoAttribute(fullAttributeName);
        return new TangoAttributeProxyImpl(tangoAttribute);
    }


    public static Optional<TangoAttributeProxy> optionalTangoAttributeProxy(String fullAttributeName){
        try {
            return Optional.of(newTangoAttributeProxy(fullAttributeName));
        } catch (DevFailed devFailed) {
            return Optional.empty();
        }
    }

    public static final Pattern TANGO_DEVICE_FULL_NAME_PATTERN = Pattern.compile("tango://(?<host>(.+):(\\d{5}))/(?<name>.+/.+/.+)");

    public static Optional<TangoDeviceProxy> optionalTangoDeviceProxy(String fullDeviceName){
        try {
            Matcher matcher = TANGO_DEVICE_FULL_NAME_PATTERN.matcher(fullDeviceName);
            if(!matcher.matches()) throw new AssertionError();
            TangoProxy tangoProxy = TangoProxies.newDeviceProxyWrapper(
                    DeviceProxyFactory.get(fullDeviceName));

            return Optional.of(new TangoDeviceProxyImpl(matcher.group("host"), matcher.group("name"), tangoProxy));
        } catch (TangoProxyException|DevFailed devFailed) {
            return Optional.empty();
        }
    }

    public static TangoDatabaseProxy getDatabase(String host, String port) throws DevFailed {
        try {
            DatabaseFactory.setUseDb(true);
            org.tango.client.database.Database obj = (org.tango.client.database.Database) DatabaseFactory.getDatabase(host, port);
            Field fldDatabase = obj.getClass().getDeclaredField("database");
            fldDatabase.setAccessible(true);

            return new TangoDatabaseProxyImpl(host, port, obj, (Database) fldDatabase.get(obj));
        } catch (NoSuchFieldException|IllegalAccessException e) {
            throw DevFailedUtils.newDevFailed(e);
        }
    }

    /**
     *
     * @param s localhost:10000
     * @return optional db
     */
    public static Optional<TangoDatabaseProxy> getDatabase(String s){
        String[] host_port = s.split(":");
        String host = host_port[0];
        String port = host_port.length == 1 ? DEFAULT_TANGO_PORT : host_port[1];
        try {
            return Optional.of(getDatabase(host, port));
        } catch (DevFailed devFailed) {
            return Optional.empty();
        }
    }

    private static TangoCommandProxy newTangoCommandProxy(String name) throws DevFailed {
        TangoCommand tangoCommand = new TangoCommand(name);
        return new TangoCommandProxyImpl(tangoCommand);
    }

    public static TangoCommandProxy newTangoCommandProxy(TangoDeviceProxy deviceProxy, String name) throws DevFailed {
        TangoCommand tangoCommand = new TangoCommand(deviceProxy.getProxy().toDeviceProxy(), name);
        return new TangoCommandProxyImpl(tangoCommand);
    }

    public static final Pattern TANGO_MEMBER_FULL_NAME_PATTERN = Pattern.compile("(?<device>tango://(?<host>(.+):(\\d{5}))/(.+/.+/.+))/(?<name>.+)");

    public static Optional<TangoCommandProxy> optionalTangoCommandProxy(String commandFullName) {
        try {
            Matcher matcher = TANGO_MEMBER_FULL_NAME_PATTERN.matcher(commandFullName);
            if(!matcher.matches()) throw new AssertionError();
            return Optional.of(newTangoCommandProxy(commandFullName));
        } catch (DevFailed devFailed) {
            return Optional.empty();
        }
    }

    public static Optional<TangoCommandProxy> optionalTangoCommandProxy(TangoDeviceProxy device, String name) {
        try {
            return Optional.of(newTangoCommandProxy(device, name));
        } catch (DevFailed devFailed) {
            return Optional.empty();
        }
    }

    public static Optional<TangoPipeProxy> optionalTangoPipeProxy(String host, String deviceName, String name) {
        try {
            DeviceProxy deviceProxy = DeviceProxyFactory.get("tango://" + host + "/" + deviceName);
            return Optional.of(new TangoPipeProxyImpl(host, deviceName, name, deviceProxy));
        } catch (DevFailed devFailed) {
            return Optional.empty();
        }

    }


    public static final Pattern TANGO_DATABASE_FULL_NAME_PATTERN = Pattern.compile("(?<host>.+):(?<port>\\d{5})");

    public static Optional<TangoDatabaseProxy> optionalTangoDatabaseProxy(String fullTangoHost) {
        Matcher matcher = TANGO_DATABASE_FULL_NAME_PATTERN.matcher(fullTangoHost);
        if(!matcher.matches()) throw new AssertionError();
        try {
            return Optional.of(getDatabase(matcher.group("host"), matcher.group("port")));
        } catch (DevFailed devFailed) {
            return Optional.empty();
        }
    }

    public static TangoDeviceProxy newTangoDeviceProxy(String host, String name) throws TangoProxyException {
        try {
            TangoProxy tangoProxy = TangoProxies.newDeviceProxyWrapper(
                    DeviceProxyFactory.get(TangoDeviceProxyUtils.toUriBuilder(host, name).build().toString()));
            return new TangoDeviceProxyImpl(host, name, tangoProxy);
        } catch (DevFailed devFailed) {
            throw TangoUtils.convertDevFailedToException(devFailed);
        }
    }
}
