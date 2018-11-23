package org.tango.web.server.proxy;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.Database;
import fr.esrf.TangoApi.DeviceProxy;
import fr.esrf.TangoApi.DeviceProxyFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11/17/18
 */
public class TangoDatabaseProxyImpl implements TangoDatabaseProxy {
    private final String host;
    private final String port;
    private final Database tangoDb;
    private final org.tango.client.database.Database soleilDb;

    public TangoDatabaseProxyImpl(String host, String port, org.tango.client.database.Database soleilDb, Database tangoDb) {
        this.host = host;
        this.port = port;
        this.soleilDb = soleilDb;
        this.tangoDb = tangoDb;
    }

    @Override
    public Database asEsrfDatabase(){
        return tangoDb;
    }

    @Override
    public org.tango.client.database.Database asSoleilDatabase(){
        return soleilDb;
    }

    @Override
    public String getTangoHost(){
        return host + ":" + port;
    }

    @Override
    public String getFullTangoHost() {
        return "tango://" + getTangoHost();
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public String getPort() {
        return port;
    }

    @Override
    public String[] getInfo() throws DevFailed {
        return tangoDb.command_inout("DbInfo").extractStringArray();
    }

    @Override
    public List<String> getDeviceNames(String wildcard) {
        try {
            return Arrays.asList(tangoDb.getDevices(wildcard));
        } catch (DevFailed devFailed) {
            return Collections.emptyList();
        }
    }

    @Override
    public List<String> getDeviceAttributeNames(String device, String wildcard) {
        try {
            return Arrays.stream(tangoDb.get_device_attribute_list(device))
                    .filter(s -> wildcard.equals("*") || s.equalsIgnoreCase(wildcard)) //TODO RegExp
                    .collect(Collectors.toList());
        } catch (DevFailed devFailed) {
            return Collections.emptyList();
        }
    }

    @Override
    public List<String> getDeviceCommandNames(String device, String wildcard) {
        DeviceProxy deviceProxy = null;
        try {
            deviceProxy = DeviceProxyFactory.get(device);
        } catch (DevFailed devFailed) {
            throw new RuntimeException(devFailed);
        }
        try {
            return Arrays.stream(deviceProxy.command_list_query())
                    .map(commandInfo -> commandInfo.cmd_name)
                    .filter(s -> wildcard.equals("*") || s.equalsIgnoreCase(wildcard)) //TODO RegExp
                    .collect(Collectors.toList());
        } catch (DevFailed devFailed) {
            return Collections.emptyList();
        }
    }

    @Override
    public List<String> getDevicePipeNames(String device, String wildcard) {
        try {
            return tangoDb.getDevicePipeList(device, wildcard);
        } catch (DevFailed devFailed) {
            return Collections.emptyList();
        }
    }

    @Override
    public String getName() {
        return tangoDb.get_name();
    }

    @Override
    public String getDeviceAlias(String device) {
        try {
            return tangoDb.get_alias_from_device(device);
        } catch (DevFailed devFailed) {
            return null;
        }
    }
}
