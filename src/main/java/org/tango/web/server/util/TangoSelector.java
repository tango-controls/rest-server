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

import org.tango.web.server.TangoProxiesCache;
import org.tango.web.server.proxy.*;

import java.util.AbstractMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11/16/18
 */
//TODO handle exceptions
public class TangoSelector {

    private final TangoProxiesCache context;
    private final List<Wildcard> wildcards;

    public TangoSelector(List<Wildcard> wildcards, TangoProxiesCache context) {
        this.wildcards = wildcards;
        this.context = context;
    }

    private Stream<WildcardDatabase> getWildcardDatabaseStream(){
        return wildcards.stream()
                .map(wildcard ->
                        Proxies.getDatabase(wildcard.host).map(tangoDatabaseProxy ->
                                new WildcardDatabase(wildcard, tangoDatabaseProxy)
                        ).orElse(null))
                .filter(Objects::nonNull);
    }

    public List<TangoDatabaseProxy> selectTangoHosts(){
        return getWildcardDatabaseStream()
                .map(WildcardDatabase::getDatabase)
                .collect(Collectors.toList());
    }

    private Stream<WildcardDevice> getWildcardDeviceStream(){
        return getWildcardDatabaseStream()
                .flatMap(wildcardDatabase ->
                        wildcardDatabase.database.getDeviceNames(wildcardDatabase.wildcard.asDeviceWildcard()).stream()
                                .map(s ->
                                        context.devices.getUnchecked(wildcardDatabase.database.getFullTangoHost() + "/" + s)
                                                .map(tangoDeviceProxy -> new WildcardDevice(wildcardDatabase,tangoDeviceProxy))
                                                .orElse(null)
                                )
                                .filter(Objects::nonNull)
                );
    }

    public List<TangoDeviceProxy> selectDevices(){
        return getWildcardDeviceStream()
                .map(WildcardDevice::getDevice)
                .collect(Collectors.toList());
    }

    public Stream<String> getDevicesURL(AbstractMap.SimpleEntry<Wildcard, TangoDatabaseProxy> entry) {
        return entry.getValue().getDeviceNames(entry.getKey().asDeviceWildcard()).stream().map(s -> "tango://" + entry.getValue().getTangoHost() + "/" + s);
    }

    private Stream<WildcardMember<TangoAttributeProxy>> getWildcardAttributeStream(){
        return getWildcardDeviceStream()
                .flatMap(wildcardDevice ->
                        wildcardDevice.device.getAttributeNames(wildcardDevice.wildcard.attribute).stream()
                                .map(s ->
                                        Proxies.optionalTangoAttributeProxy(wildcardDevice.device.getFullName() + "/" + s)
                                                .map(tangoAttributeProxy -> new WildcardMember<>(wildcardDevice, tangoAttributeProxy))
                                                .orElse(null)
                                )
                                .filter(Objects::nonNull)
                );
    }

    public Stream<TangoAttributeProxy> selectAttributesStream(){
        return getWildcardAttributeStream()
                .map(WildcardMember::getMember);
    }

    private Stream<WildcardMember<TangoCommandProxy>> getWildcardCommandStream(){
        return getWildcardDeviceStream()
                .flatMap(wildcardDevice ->
                        wildcardDevice.device.getCommandNames(wildcardDevice.wildcard.attribute).stream()
                                .map(s ->
                                        Proxies.optionalTangoCommandProxy(wildcardDevice.device, s)
                                                .map(tangoCommandProxy -> new WildcardMember<>(wildcardDevice, tangoCommandProxy))
                                                .orElse(null)
                                )
                                .filter(Objects::nonNull)
                );
    }


    public Stream<TangoCommandProxy> selectCommandsStream(){
        return getWildcardCommandStream()
                .map(WildcardMember::getMember);
    }

    private Stream<WildcardMember<TangoPipeProxy>> getWildcardPipeStream(){
        return getWildcardDeviceStream()
                .flatMap(wildcardDevice ->
                        wildcardDevice.device.getPipeNames(wildcardDevice.wildcard.attribute).stream()
                                .map(s ->
                                        Proxies.optionalTangoPipeProxy(
                                                wildcardDevice.getDatabase().getTangoHost(),
                                                wildcardDevice.getDevice().getName(),
                                                s)
                                                .map(tangoCommandProxy -> new WildcardMember<>(wildcardDevice, tangoCommandProxy))
                                                .orElse(null)
                                )
                                .filter(Objects::nonNull)
                );
    }

    public Stream<TangoPipeProxy> selectPipesStream(){
        return getWildcardPipeStream()
                .map(WildcardMember::getMember);
    }


    private Stream<String> getDeviceMemberURLStream() {
        return wildcards.stream()
                .map(wildcard -> new AbstractMap.SimpleEntry<>(wildcard, Proxies.getDatabase(wildcard.host).orElse(null)))
                .filter(entry -> Objects.nonNull(entry.getValue()))
                .flatMap(this::getDeviceMemberURL);
    }

    private Stream<String> getDeviceMemberURL(AbstractMap.SimpleEntry<Wildcard, TangoDatabaseProxy> wildcardTangoDatabaseSimpleEntry) {
        return getDevicesURL(wildcardTangoDatabaseSimpleEntry).map(s -> s + "/" + wildcardTangoDatabaseSimpleEntry.getKey().attribute);
    }

    private static class WildcardDatabase {
        Wildcard wildcard;
        TangoDatabaseProxy database;

        public WildcardDatabase(Wildcard wildcard, TangoDatabaseProxy database) {
            this.wildcard = wildcard;
            this.database = database;
        }

        public Wildcard getWildcard() {
            return wildcard;
        }

        public TangoDatabaseProxy getDatabase() {
            return database;
        }
    }

    private static class WildcardDevice extends WildcardDatabase {
        TangoDeviceProxy device;

        public WildcardDevice(WildcardDatabase wildcard, TangoDeviceProxy device) {
            super(wildcard.wildcard, wildcard.database);
            this.device = device;
        }

        public TangoDeviceProxy getDevice() {
            return device;
        }
    }

    private static class WildcardMember<T> extends WildcardDevice {
        T member;

        public WildcardMember(WildcardDevice wildcard, T member) {
            super(wildcard, wildcard.device);
            this.member = member;
        }

        public T getMember() {
            return member;
        }
    }
}