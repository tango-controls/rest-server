package org.tango.web.server.rest;

import javax.annotation.concurrent.NotThreadSafe;

/**
 * Mirrors {@link fr.esrf.TangoApi.CommandInfo}
 *
 * @author Ingvord
 * @since 06.07.14
 */
@NotThreadSafe
public class CommandInfo {
    public String cmd_name;
    //    public Object level;
    public String cmd_tag;
    public int in_type;
    public int out_type;
    public String in_type_desc;
    public String out_type_desc;
}
