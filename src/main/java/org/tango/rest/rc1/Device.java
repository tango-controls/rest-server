package org.tango.rest.rc1;

import org.tango.web.rest.DeviceInfo;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 27.11.2015
 */
public class Device {
    public final String name;
    public final DeviceInfo info = null;
    public final String href;

    public Device(String name, String href) {
        this.name = name;
        this.href = href;
    }
}
