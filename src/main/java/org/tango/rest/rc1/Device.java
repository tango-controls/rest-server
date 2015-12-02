package org.tango.rest.rc1;


import fr.esrf.TangoApi.DeviceInfo;

import java.util.Collection;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 27.11.2015
 */
public class Device {
    public final String name;
    public final DeviceInfo info;
    public final Collection<NamedEntity> attributes;
    public final Collection<NamedEntity> commands;
    public final Collection<NamedEntity> properties;
    public final String state;
    public final Object _links;

    public Device(String name, DeviceInfo info, Collection<NamedEntity> attributes, Collection<NamedEntity> commands, Collection<NamedEntity> properties, Object href) {
        this.name = name;
        this.info = info;
        this.attributes = attributes;
        this.commands = commands;
        this.properties = properties;
        this._links = href;
        this.state = href + "/state";
    }
}
