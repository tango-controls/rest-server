package org.tango.rest;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 8/6/16
 */
public class DeviceHelper {
    public static Object attributeInfoExToResponse(final String attrName, final String href) {
        return new Object() {
            public String name = attrName;
            public String value = href + "/value";
            public String info = href + "/info";
            public String properties = href + "/properties";
            public String history = href + "/history";
            public Object _links = new Object() {
                public String _parent = href;
                //TODO use LinksProvider
            };
        };
    }
}
