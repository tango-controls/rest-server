package org.tango.web.server.proxy;

import org.tango.client.ez.proxy.TangoProxy;

import java.util.List;

/**
 * @author ingvord
 * @since 11/18/18
 */
public interface TangoDeviceProxy {
    String getName();

    String getAlias();

    String getFullName();

    String getHost();

    TangoProxy getProxy();

    List<String> getAttributeNames(String wildcard);

    List<String> getCommandNames(String wildcard);

    List<String> getPipeNames(String wildcard);
}
