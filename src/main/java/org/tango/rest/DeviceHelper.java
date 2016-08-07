package org.tango.rest;

import fr.esrf.TangoApi.DeviceInfo;
import org.tango.web.server.util.DeviceInfos;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 8/6/16
 */
public class DeviceHelper {
    public static Object deviceToResponse(String devname, final DeviceInfo info,String href){
        return new org.tango.rest.entities.Device(devname,
                DeviceInfos.fromDeviceInfo(info),
                href + "/attributes",
                href + "/commands",
                href + "/pipes",
                href + "/properties", href);
    }

    public static Object attributeInfoExToResponse(final String attrName, final String href) {
        return new Object() {
            public String name = attrName;
            public String value = href + "/value";
            public String info = href + "/info";
            public String properties = href + "/properties";
            public String history = href + "/history";
            public Object _links = new Object() {
                public String _self = href;
                //TODO use LinksProvider
            };
        };
    }
}
