package org.tango.web.server.proxy;

import org.tango.client.ez.proxy.TangoProxy;

import javax.ws.rs.core.UriBuilder;

/**
 * @author ingvord
 * @since 11/18/18
 */
public class TangoDeviceProxy {
    public final TangoDatabase database;
    public final String name;
    public final TangoProxy proxy;

    public TangoDeviceProxy(TangoDatabase database, String name, TangoProxy proxy) {
        this.database = database;
        this.name = name;
        this.proxy = proxy;
    }

    public UriBuilder toUriBuilder(){
        return UriBuilder.fromPath("tango://").path(database.getFullTangoHost()).path(name);
    }
}
