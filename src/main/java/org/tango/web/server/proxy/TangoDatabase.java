package org.tango.web.server.proxy;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.Connection;
import fr.esrf.TangoApi.Database;
import fr.esrf.TangoApi.DeviceData;
import fr.esrf.TangoApi.IConnectionDAO;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11/17/18
 */
public class TangoDatabase {
    private final String host;
    private final String port;
    private final Database tangoDb;
    private final org.tango.client.database.Database soleilDb;

    public TangoDatabase(String host, String port, org.tango.client.database.Database soleilDb, Database tangoDb) {
        this.host = host;
        this.port = port;
        this.soleilDb = soleilDb;
        this.tangoDb = tangoDb;
    }

    public Database asEsrfDb(){
        return tangoDb;
    }

    public org.tango.client.database.Database asSoleilDb(){
        return soleilDb;
    }

    public String getFullTangoHost(){
        return host + ":" + port;
    }

    public String getHost() {
        return host;
    }

    public String getPort() {
        return port;
    }

    public String[] getInfo() throws DevFailed {
        return tangoDb.command_inout("DbInfo").extractStringArray();
    }

    public List<String> getDevices(String wildcard) {
        try {
            return Arrays.asList(tangoDb.getDevices(wildcard));
        } catch (DevFailed devFailed) {
            return Collections.emptyList();
        }
    }
}
