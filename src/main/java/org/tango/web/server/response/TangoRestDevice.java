package org.tango.web.server.response;

import fr.esrf.TangoApi.DeviceInfo;
import org.tango.rest.entities.Device;

import java.net.URI;

/**
 * @author ingvord
 * @since 11/18/18
 */
public class TangoRestDevice extends Device {
    public DeviceInfo info;

    public TangoRestDevice(String id, String name, String host, DeviceInfo info, URI href) {
        super(id, name, host, null, href);
        this.info = info;
    }
}
