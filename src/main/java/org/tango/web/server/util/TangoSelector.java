package org.tango.web.server.util;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DeviceProxy;
import fr.esrf.TangoApi.DeviceProxyFactory;
import fr.esrf.TangoApi.PipeInfo;
import fr.soleil.tango.clientapi.TangoAttribute;
import fr.soleil.tango.clientapi.TangoCommand;
import org.tango.client.ez.proxy.TangoProxies;
import org.tango.client.ez.proxy.TangoProxy;
import org.tango.client.ez.proxy.TangoProxyException;
import org.tango.web.server.proxy.TangoDatabase;
import org.tango.web.server.proxy.TangoPipeProxy;
import org.tango.web.server.proxy.TangoPipeProxyImpl;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11/16/18
 */
//TODO handle exceptions
public class TangoSelector {

    private final List<Wildcard> wildcards;

    public TangoSelector(List<Wildcard> wildcards) {
        this.wildcards = wildcards;
    }

    public List<TangoDatabase> selectTangoHosts(){
        return wildcards.stream()
                .map(wildcard -> TangoDatabaseUtils.getDatabase(wildcard.host))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    public List<Optional<TangoProxy>> selectDevices(){
        return wildcards.stream()
                .map(wildcard -> new AbstractMap.SimpleEntry<>(wildcard,TangoDatabaseUtils.getDatabase(wildcard.host).orElse(null)  ))
                .filter(entry -> Objects.nonNull(entry.getValue()))
                .flatMap(this::getDevicesURL)
                .map(s -> {
                    try {
                        return Optional.of(TangoProxies.newDeviceProxyWrapper(s));
                    } catch (TangoProxyException e) {
                        return Optional.<TangoProxy>empty();
                    }
                })
                .collect(Collectors.toList());
    }

    public Stream<String> getDevicesURL(AbstractMap.SimpleEntry<Wildcard, TangoDatabase> entry) {
        return entry.getValue().getDevices(entry.getKey().asDeviceWildcard()).stream().map(s -> "tango://" + entry.getValue().getFullTangoHost() + "/" + s);
    }

    public List<Optional<TangoAttribute>> selectAttributes(){
        return getDeviceMemberURLStream()
                .map(s -> {
                    try {
                        return Optional.of(new TangoAttribute(s));
                    } catch (DevFailed devFailed) {
                        return Optional.<TangoAttribute>empty();
                    }
                })
                .collect(Collectors.toList());
    }

    public List<Optional<TangoCommand>> selectCommands(){
        return getDeviceMemberURLStream()
                .map(s -> {
                    try {
                        return Optional.of(new TangoCommand(s.substring(0, s.lastIndexOf('/')), s.substring(s.lastIndexOf('/') + 1)));
                    } catch (DevFailed devFailed) {
                        return Optional.<TangoCommand>empty();
                    }
                })
                .collect(Collectors.toList());
    }

    public Stream<Optional<TangoPipeProxy>> selectPipes(){
        return getDeviceMemberURLStream()
                .map(s -> {
                    try {
                        DeviceProxy proxy = DeviceProxyFactory.get(s.substring(0,s.lastIndexOf('/')));
                        return Optional.<TangoPipeProxy>of(new TangoPipeProxyImpl(s.substring(s.lastIndexOf('/') + 1), proxy));
                    } catch (DevFailed devFailed) {
                        return Optional.<TangoPipeProxy>empty();
                    }
                });
    }


    private Stream<String> getDeviceMemberURLStream() {
        return wildcards.stream()
                .map(wildcard -> new AbstractMap.SimpleEntry<>(wildcard, TangoDatabaseUtils.getDatabase(wildcard.host).orElse(null)))
                .filter(entry -> Objects.nonNull(entry.getValue()))
                .flatMap(this::getDeviceMemberURL);
    }

    private Stream<String> getDeviceMemberURL(AbstractMap.SimpleEntry<Wildcard, TangoDatabase> wildcardTangoDatabaseSimpleEntry) {
        return getDevicesURL(wildcardTangoDatabaseSimpleEntry).map(s -> s + "/" + wildcardTangoDatabaseSimpleEntry.getKey().attribute);
    }
}
