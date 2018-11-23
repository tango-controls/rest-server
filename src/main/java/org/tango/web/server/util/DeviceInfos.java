package org.tango.web.server.util;

import org.tango.rest.rc4.entities.DeviceInfo;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 17.12.2015
 */
public class DeviceInfos {
    private DeviceInfos(){}


    public static DeviceInfo fromDeviceInfo(fr.esrf.TangoApi.DeviceInfo deviceInfo){
        DeviceInfo instance = new DeviceInfo();

        instance.last_exported = deviceInfo.last_exported;
        instance.last_unexported = deviceInfo.last_unexported;
        instance.name = deviceInfo.name;
        instance.ior = deviceInfo.ior;
        instance.version = deviceInfo.version;
        instance.exported = deviceInfo.exported;
        instance.pid = deviceInfo.pid;
        instance.server = deviceInfo.server;
        instance.hostname = deviceInfo.hostname;
        instance.classname = deviceInfo.classname;
        instance.is_taco = deviceInfo.is_taco;

        return instance;
    }
}
