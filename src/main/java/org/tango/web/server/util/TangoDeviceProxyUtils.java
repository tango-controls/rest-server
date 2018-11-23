package org.tango.web.server.util;

import org.tango.client.ez.proxy.TangoProxy;
import org.tango.web.server.proxy.TangoDeviceProxy;
import org.tango.web.server.proxy.TangoDeviceProxyImpl;

import javax.ws.rs.core.UriBuilder;
import java.util.Optional;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11/19/18
 */
public class TangoDeviceProxyUtils {
    private TangoDeviceProxyUtils(){}

    public static UriBuilder toUriBuilder(String fullName){
        return UriBuilder.fromPath("tango://").path(fullName);
    }

    public static UriBuilder toUriBuilder(String host, String name){
        return toUriBuilder(host + "/" + name);
    }
}
