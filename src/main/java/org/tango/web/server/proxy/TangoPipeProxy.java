package org.tango.web.server.proxy;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DevicePipe;
import fr.esrf.TangoApi.DeviceProxy;
import fr.esrf.TangoApi.PipeBlob;
import org.tango.rest.entities.pipe.PipeInfo;

/**
 * @author ingvord
 * @since 11/19/18
 */
public interface TangoPipeProxy {
    String getTangoHost() throws DevFailed;

    String getName();

    DeviceProxy getDeviceProxy();

    PipeInfo getInfo() throws DevFailed;

    DevicePipe read() throws DevFailed;

    void write(PipeBlob data) throws DevFailed;
}
