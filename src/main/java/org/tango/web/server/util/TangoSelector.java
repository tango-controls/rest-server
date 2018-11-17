package org.tango.web.server.util;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.Database;
import fr.soleil.tango.clientapi.TangoAttribute;
import org.codehaus.jackson.map.ObjectMapper;
import org.tango.client.ez.proxy.TangoProxies;
import org.tango.client.ez.proxy.TangoProxy;
import org.tango.client.ez.proxy.TangoProxyException;
import org.tango.rest.entities.Attribute;
import org.tango.rest.entities.Device;
import org.tango.rest.entities.TangoHost;

import javax.swing.text.html.Option;
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
        return wildcards.stream()
                .map(wildcard -> new AbstractMap.SimpleEntry<>(wildcard, TangoDatabaseUtils.getDatabase(wildcard.host).orElse(null)))
                .filter(entry -> Objects.nonNull(entry.getValue()))
                .flatMap(this::getDeviceAtrtibutesURL)
                .map(s -> {
                    try {
                        return Optional.of(new TangoAttribute(s));
                    } catch (DevFailed devFailed) {
                        return Optional.<TangoAttribute>empty();
                    }
                })
                .collect(Collectors.toList());
    }

    private Stream<String> getDeviceAtrtibutesURL(AbstractMap.SimpleEntry<Wildcard, TangoDatabase> wildcardTangoDatabaseSimpleEntry) {
        return getDevicesURL(wildcardTangoDatabaseSimpleEntry).map(s -> s + "/" + wildcardTangoDatabaseSimpleEntry.getKey().attribute);
    }
}
