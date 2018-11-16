package org.tango.web.server.util;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.Database;
import org.tango.client.database.DatabaseFactory;

import javax.swing.text.html.Option;
import java.lang.reflect.Field;
import java.util.Optional;

import static org.tango.web.server.providers.TangoDatabaseProvider.DEFAULT_TANGO_PORT;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11/15/18
 */
public class TangoDatabaseUtils {
    private TangoDatabaseUtils(){}

    public static Optional<Database> getDatabase(String host, String port){
        try {
            Object obj = DatabaseFactory.getDatabase(host, port);
            Field fldDatabase = obj.getClass().getDeclaredField("database");
            fldDatabase.setAccessible(true);
            return Optional.of((Database) fldDatabase.get(obj));
        } catch (DevFailed|NoSuchFieldException|IllegalAccessException e) {
            return Optional.empty();
        }
    }

    /**
     *
     * @param s localhost:10000
     * @return optional db
     */
    public static Optional<Database> getDatabase(String s){
        String[] host_port = s.split(":");
        String host = host_port[0];
        String port = host_port.length == 1 ? DEFAULT_TANGO_PORT : host_port[1];
        return getDatabase(host, port);
    }
}
