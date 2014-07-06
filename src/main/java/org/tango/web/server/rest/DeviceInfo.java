package org.tango.web.server.rest;

import javax.annotation.concurrent.NotThreadSafe;

/**
 * Mirrors {@link fr.esrf.TangoApi.DeviceInfo}
 *
 * @author Ingvord
 * @since 06.07.14
 */
@NotThreadSafe
public class DeviceInfo {
    public String last_exported;
    public String last_unexported;
    public String name;
    public String ior;
    public int version;
    public boolean exported;
    public int pid;
    public String server;
    public String hostname;
    public String classname;
    public boolean isTaco;
}
