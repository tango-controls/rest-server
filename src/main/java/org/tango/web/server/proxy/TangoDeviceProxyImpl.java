package org.tango.web.server.proxy;

import fr.esrf.Tango.DevFailed;
import org.tango.client.ez.proxy.TangoProxy;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author ingvord
 * @since 11/18/18
 */
public class TangoDeviceProxyImpl implements TangoDeviceProxy {
    private final String name;
    private final TangoProxy proxy;
    private final String fullName;

    public TangoDeviceProxyImpl(String host, String name, TangoProxy proxy) {
        this.fullName = "tango://" + host + "/" + name;
        this.name = name;
        this.proxy = proxy;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getAlias() {
        try {
            return proxy.toDeviceProxy().get_alias();
        } catch (DevFailed devFailed) {
            return null;
        }
    }

    @Override
    public String getFullName() {
        return fullName;
    }

    @Override
    public TangoProxy getProxy() {
        return proxy;
    }

    @Override
    public List<String> getAttributeNames(String wildcard) {
        try {
            return Arrays.stream(proxy.toDeviceProxy().get_attribute_list())
                    .filter(s -> wildcard.equals("*") || s.equalsIgnoreCase(wildcard))
                    .collect(Collectors.toList());
        } catch (DevFailed devFailed) {
            return Collections.emptyList();
        }
    }

    @Override
    public List<String> getCommandNames(String wildcard) {
        try {
            return Arrays.stream(proxy.toDeviceProxy().command_list_query())
                    .map(commandInfo -> commandInfo.cmd_name)
                    .filter(s -> wildcard.equals("*") || s.equalsIgnoreCase(wildcard))
                    .collect(Collectors.toList());
        } catch (DevFailed devFailed) {
            return Collections.emptyList();
        }
    }

    @Override
    public List<String> getPipeNames(String wildcard) {
        try {
            return proxy.toDeviceProxy().getPipeNames().stream()
                    .filter(s -> wildcard.equals("*") || s.equalsIgnoreCase(wildcard))
                    .collect(Collectors.toList());
        } catch (DevFailed devFailed) {
            return Collections.emptyList();
        }
    }
}
