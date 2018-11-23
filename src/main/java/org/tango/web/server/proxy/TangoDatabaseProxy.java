package org.tango.web.server.proxy;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.Database;

import java.util.List;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11/19/18
 */
public interface TangoDatabaseProxy {
    Database asEsrfDatabase();

    org.tango.client.database.Database asSoleilDatabase();

    String getTangoHost();

    String getFullTangoHost();

    String getHost();

    String getPort();

    String[] getInfo() throws DevFailed;

    List<String> getDeviceNames(String wildcard);

    List<String> getDeviceAttributeNames(String device, String wildcard);

    List<String> getDeviceCommandNames(String device, String wildcard);

    List<String> getDevicePipeNames(String device, String wildcard);

    String getName();

    String getDeviceAlias(String device);
}
