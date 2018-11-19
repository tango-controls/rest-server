package org.tango.web.server.proxy;

import org.tango.client.ez.proxy.TangoProxy;

import javax.ws.rs.core.UriBuilder;
import java.util.Arrays;
import java.util.List;

/**
 * @author ingvord
 * @since 11/18/18
 */
public interface TangoDeviceProxy {
    String getName();

    String getFullName();

    TangoProxy getProxy();

    List<String> getAttributeNames(String wildcard);

    List<String> getCommandNames(String wildcard);

    List<String> getPipeNames(String wildcard);
}
