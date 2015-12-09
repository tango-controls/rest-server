package org.tango.rest.rc1;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 09.12.2015
 */
public class AttributeValue {
    public String name;
    public Object value;
    public String quality;
    public long timestamp;
    public Object _links;

    public AttributeValue(String name, Object value, String quality, long timestamp, final String __self, final String __device) {
        this.name = name;
        this.value = value;
        this.quality = quality;
        this.timestamp = timestamp;
        this._links = new Object(){
            public String _self = __self;
            public String _device = __device;
        };
    }
}
