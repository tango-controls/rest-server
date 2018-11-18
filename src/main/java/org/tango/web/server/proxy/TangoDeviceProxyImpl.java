package org.tango.web.server.proxy;

import org.tango.client.ez.proxy.TangoProxy;

import javax.ws.rs.core.UriBuilder;

/**
 * @author ingvord
 * @since 11/18/18
 */
public class TangoDeviceProxyImpl implements TangoDeviceProxy {
    private final TangoDatabase database;
    private final String name;
    private final TangoProxy proxy;

    public TangoDeviceProxyImpl(TangoDatabase database, String name, TangoProxy proxy) {
        this.database = database;
        this.name = name;
        this.proxy = proxy;
    }

    @Override
    public TangoDatabase getDatabase() {
        return database;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public TangoProxy getProxy() {
        return proxy;
    }

    @Override
    public UriBuilder toUriBuilder(){
        return UriBuilder.fromPath("tango://").path(database.getFullTangoHost()).path(name);
    }
}
