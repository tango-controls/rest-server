package org.tango.web.server.providers;

import com.google.common.collect.Lists;
import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.Database;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.tango.client.database.DatabaseFactory;
import org.tango.web.server.binding.RequiresDeviceTreeContext;
import org.tango.web.server.tree.DeviceFilters;
import org.tango.web.server.tree.DevicesTreeContext;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11/14/18
 */
@Provider
@RequiresDeviceTreeContext
public class DevicesTreeContextProvider implements ContainerRequestFilter {
    @Override
    public void filter(ContainerRequestContext requestContext) {
        UriInfo uriInfo = requestContext.getUriInfo();

        List<String> tango_hosts = uriInfo.getQueryParameters(true).get("v");

        List<Database> dbs = tango_hosts.stream()
                .filter(this::checkURISyntax)
                .map(this::createDatabase)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());


        List<String> filters = uriInfo.getQueryParameters(true).get("f");

        DeviceFilters df = new DeviceFilters(filters);

        DevicesTreeContext context = new DevicesTreeContext(dbs, df);


        ResteasyProviderFactory.pushContext(DevicesTreeContext.class, context);
    }

    private Database createDatabase(String s) {
        String[] host_port = s.split(":");
        try {
            Object obj = DatabaseFactory.getDatabase(host_port[0], host_port[1]);
            Field fldDatabase = obj.getClass().getDeclaredField("database");
            fldDatabase.setAccessible(true);
            return (Database) fldDatabase.get(obj);
        } catch (DevFailed |IllegalAccessException|NoSuchFieldException devFailed) {
            return null;
        }
    }

    private boolean checkURISyntax(String next) {
        try {
            new URI("tango://" + next);
            return true;
        } catch (URISyntaxException e) {
            return false;
        }
    }
}
