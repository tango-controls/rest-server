package org.tango.web.server;

import fr.esrf.Tango.DevVarLongStringArray;
import hzg.wpn.tango.client.proxy.TangoProxies;
import hzg.wpn.tango.client.proxy.TangoProxy;
import hzg.wpn.tango.client.proxy.TangoProxyException;

import javax.annotation.concurrent.ThreadSafe;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 23.05.14
 */
@ThreadSafe
public class DatabaseDs {
    public static final String DEFAULT_ID = "sys/database/2";

    private final String tangoHost;
    private final TangoProxy proxy;

    public DatabaseDs(String tangoHost) throws TangoProxyException {
        this(tangoHost, DEFAULT_ID);
    }

    public DatabaseDs(String tangoHost, String devname) throws TangoProxyException {
        this.tangoHost = tangoHost;
        this.proxy = TangoProxies.newDeviceProxyWrapper("tango://" + tangoHost + "/" + devname);
    }

    public String getDeviceAddress(String devname) throws TangoProxyException {
        DevVarLongStringArray info = proxy.executeCommand("DbGetDeviceInfo", devname);
        return "tango://" + tangoHost + "/" + info.svalue[0];
    }
}
