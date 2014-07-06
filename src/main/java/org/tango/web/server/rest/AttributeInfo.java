package org.tango.web.server.rest;

import javax.annotation.concurrent.NotThreadSafe;

/**
 * Mirrors {@link fr.esrf.TangoApi.AttributeInfo}
 *
 * @author Ingvord
 * @since 06.07.14
 */
@NotThreadSafe
public class AttributeInfo {
    public String name;
    //    public Object writable;
//    public Object data_format;
    public int data_type;
    public int max_dim_x;
    public int max_dim_y;
    public String description;
    public String label;
    public String unit;
    public String standard_unit;
    public String display_unit;
    public String format;
    //    public String min_value;
//    public String max_value;
//    public String min_alarm;
//    public String max_alarm;
    public String writable_attr_name;
    //    public Object level;
    public Object extensions;

}
