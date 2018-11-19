package org.tango.web.server.proxy;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DevicePipe;
import fr.esrf.TangoApi.DeviceProxy;
import fr.esrf.TangoApi.PipeBlob;
import org.tango.rest.entities.pipe.DispLevel;
import org.tango.rest.entities.pipe.PipeInfo;
import org.tango.rest.entities.pipe.PipeWriteType;

/**
 * @author ingvord
 * @since 11/19/18
 */
public class TangoPipeProxyImpl implements TangoPipeProxy {
    private final String name;
    private final DeviceProxy deviceProxy;

    public TangoPipeProxyImpl(String name, DeviceProxy deviceProxy) {
        this.name = name;
        this.deviceProxy = deviceProxy;
    }

    @Override
    public String getTangoHost() throws DevFailed{
        return deviceProxy.getFullTangoHost();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public DeviceProxy getDeviceProxy() {
        return deviceProxy;
    }

    @Override
    public PipeInfo getInfo() throws DevFailed {
        fr.esrf.TangoApi.PipeInfo pipeConfig = deviceProxy.getPipeConfig(name);
        return new org.tango.rest.entities.pipe.PipeInfo(pipeConfig.getName(), pipeConfig.getDescription(), pipeConfig.getLabel(), new DispLevel(pipeConfig.getLevel()), new PipeWriteType(pipeConfig.getWriteType()));
    }

    @Override
    public DevicePipe read() throws DevFailed {
        return deviceProxy.readPipe(name);
    }

    @Override
    public void write(PipeBlob blob) throws DevFailed {
        deviceProxy.writePipe(name, blob);
    }
}
