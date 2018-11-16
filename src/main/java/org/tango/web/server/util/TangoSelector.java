package org.tango.web.server.util;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.Database;
import org.tango.client.ez.proxy.TangoProxies;
import org.tango.client.ez.proxy.TangoProxy;
import org.tango.rest.entities.Attribute;
import org.tango.rest.entities.Device;
import org.tango.rest.entities.TangoHost;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11/16/18
 */
public class TangoSelector {

    private final List<Wildcard> wildcards;

    public TangoSelector(List<Wildcard> wildcards) {
        this.wildcards = wildcards;
    }

    public List<TangoHost> selectTangoHosts(){
        return Collections.emptyList();
    }

    public List<Device> selectDevices(){
        return Collections.emptyList();
    }

    public List<Attribute> selectAttributes(){
        return wildcards.stream().map(wildcard -> TangoDatabaseUtils.getDatabase(wildcard.host))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .flatMap(database -> database.getDevices(wildcard.asDeviceWildcard()))
                .flatMap(s -> database.get_device_attribute_list(s))
                .filter(s -> wildcard.attribute.equalsIgnoreCase(s))
                .map()
                .collect(Collectors.toList());
    }
}
