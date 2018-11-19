package org.tango.web.server.proxy;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.Database;
import fr.esrf.TangoApi.DeviceProxy;
import fr.esrf.TangoApi.DeviceProxyFactory;
import fr.soleil.tango.clientapi.TangoAttribute;
import fr.soleil.tango.clientapi.TangoCommand;
import org.tango.client.database.DatabaseFactory;
import org.tango.client.ez.proxy.TangoProxies;
import org.tango.client.ez.proxy.TangoProxy;
import org.tango.client.ez.proxy.TangoProxyException;
import org.tango.utils.DevFailedUtils;
import org.tango.web.server.TangoProxyPool;
import org.tango.web.server.util.TangoDeviceProxyUtils;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.tango.web.server.providers.TangoDatabaseProvider.DEFAULT_TANGO_PORT;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11/19/18
 */
public class Proxies {
    private Proxies(){}

    public static TangoAttributeProxy newTangoAttributeProxy(String fullAttributeName) throws DevFailed {
        TangoAttribute tangoAttribute = new TangoAttribute(fullAttributeName);
        return new TangoAttributeProxyImpl(tangoAttribute);
    }


    public static Optional<TangoAttributeProxy> optionalTangoAttributeProxy(String fullAttributeName){
        try {
            return Optional.of(newTangoAttributeProxy(fullAttributeName));
        } catch (DevFailed devFailed) {
            return Optional.empty();
        }
    }

    public static Optional<TangoDeviceProxy> optionalTangoDeviceProxy(String host, String name){
        try {
            TangoProxy tangoProxy = TangoProxies.newDeviceProxyWrapper(
                    DeviceProxyFactory.get(TangoDeviceProxyUtils.toUriBuilder(host, name).build().toString()));
            return Optional.of(new TangoDeviceProxyImpl(host, name, tangoProxy));
        } catch (TangoProxyException|DevFailed devFailed) {
            return Optional.empty();
        }
    }

    public static Optional<TangoDeviceProxy> optionalTangoDeviceProxyFromPool(String host, String name, TangoProxyPool pool){
        try {
            TangoProxy tangoProxy = pool.getProxy(TangoDeviceProxyUtils.toUriBuilder(host, name).build().toString());
            return Optional.of(new TangoDeviceProxyImpl(host, name, tangoProxy));
        } catch (TangoProxyException e) {
            return Optional.empty();
        }
    }

    public static TangoDatabaseProxy getDatabase(String host, String port) throws DevFailed {
        try {
            DatabaseFactory.setUseDb(true);
            org.tango.client.database.Database obj = (org.tango.client.database.Database) DatabaseFactory.getDatabase(host, port);
            Field fldDatabase = obj.getClass().getDeclaredField("database");
            fldDatabase.setAccessible(true);

            return new TangoDatabaseProxyImpl(host, port, obj, (Database) fldDatabase.get(obj));
        } catch (NoSuchFieldException|IllegalAccessException e) {
            throw DevFailedUtils.newDevFailed(e);
        }
    }

    /**
     *
     * @param s localhost:10000
     * @return optional db
     */
    public static Optional<TangoDatabaseProxy> getDatabase(String s){
        String[] host_port = s.split(":");
        String host = host_port[0];
        String port = host_port.length == 1 ? DEFAULT_TANGO_PORT : host_port[1];
        try {
            return Optional.of(getDatabase(host, port));
        } catch (DevFailed devFailed) {
            return Optional.empty();
        }
    }

    public static TangoCommandProxy newTangoCommandProxy(String deviceFullName, String name) throws DevFailed {
        TangoCommand tangoCommand = new TangoCommand(deviceFullName, name);
        return new TangoCommandProxyImpl(tangoCommand);
    }

    public static Optional<TangoCommandProxy> optionalTangoCommandProxy(String deviceFullName, String name) {
        try {
            return Optional.of(newTangoCommandProxy(deviceFullName, name));
        } catch (DevFailed devFailed) {
            return Optional.empty();
        }
    }

    public static Optional<TangoPipeProxy> optionalTangoPipeProxy(String deviceFullName, String name) {
        try {
            DeviceProxy deviceProxy = DeviceProxyFactory.get(deviceFullName);
            return Optional.of(new TangoPipeProxyImpl(name, deviceProxy));
        } catch (DevFailed devFailed) {
            return Optional.empty();
        }

    }


}
