package org.tango.web.server.proxy;

import org.tango.client.ez.proxy.TangoProxy;

import javax.ws.rs.core.UriBuilder;

/**
 * @author ingvord
 * @since 11/18/18
 */
public interface TangoDeviceProxy {
    TangoDatabase getDatabase();

    String getName();

    TangoProxy getProxy();

    UriBuilder toUriBuilder();
}
