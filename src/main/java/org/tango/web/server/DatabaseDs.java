package org.tango.web.server;

import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevVarLongStringArray;
import fr.esrf.TangoApi.DeviceInfo;
import org.tango.client.ez.proxy.NoSuchCommandException;
import org.tango.client.ez.proxy.TangoProxies;
import org.tango.client.ez.proxy.TangoProxy;
import org.tango.client.ez.proxy.TangoProxyException;
import org.tango.client.ez.util.TangoUtils;

import javax.annotation.concurrent.ThreadSafe;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 23.05.14
 */
@ThreadSafe
public class DatabaseDs {
    public static final String TANGO_DB = "tango.db";

    private final String tangoHost;
    private final TangoProxy proxy;

    public DatabaseDs(TangoProxy dbProxy) throws TangoProxyException{
        try {
            tangoHost = dbProxy.toDeviceProxy().getFullTangoHost();
            proxy = dbProxy;
        } catch (DevFailed devFailed) {
            throw new TangoProxyException(dbProxy.getName(), devFailed);
        }
    }

    public DeviceInfo getDeviceInfo(String devname) throws TangoProxyException, NoSuchCommandException {
        DevVarLongStringArray info = proxy.executeCommand("DbGetDeviceInfo", devname);
        DeviceInfo deviceInfo = new DeviceInfo(info);
        return deviceInfo;
    }

    public String getDeviceAddress(String devname) throws TangoProxyException, NoSuchCommandException {
        DeviceInfo info = getDeviceInfo(devname);
        return "tango://" + tangoHost + "/" + info.name;
    }

    public List<String> getDeviceList() throws TangoProxyException, NoSuchCommandException {
        String[] result = proxy.executeCommand("DbGetDeviceWideList", "*");
        return Arrays.asList(result);
    }

    public List<String> getDomainsList() throws TangoProxyException, NoSuchCommandException {
        String[] result = proxy.executeCommand("DbGetDeviceDomainList","*");
        return Arrays.asList(result);
    }

    public List<String> getFamiliesList(String domain) throws TangoProxyException, NoSuchCommandException {
        String[] result = proxy.executeCommand("DbGetDeviceFamilyList", domain + "/*");
        return Arrays.asList(result);
    }

    public List<String> getMembersList(String domain, String family) throws TangoProxyException, NoSuchCommandException {
        String[] result = proxy.executeCommand("DbGetDeviceMemberList", domain + "/" + family + "/*");
        return Arrays.asList(result);
    }

    public List<String> getDeviceList(String wildcard) throws TangoProxyException, NoSuchCommandException {
        String[] result = proxy.executeCommand("DbGetDeviceWideList", wildcard);
        return Arrays.asList(result);
    }

    public String getDbURL(){
        return "tango://" + tangoHost + "/" + proxy.getName();
    }
}
