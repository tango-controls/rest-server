package org.tango.web.server.providers;

import com.google.common.collect.Lists;
import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.Database;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.tango.client.database.DatabaseFactory;
import org.tango.rest.entities.Failures;
import org.tango.web.server.DatabaseDs;
import org.tango.web.server.binding.RequiresDeviceTreeContext;
import org.tango.web.server.tree.DeviceFilters;
import org.tango.web.server.tree.DevicesTreeContext;
import org.tango.web.server.util.TangoDatabaseUtils;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11/14/18
 */
@Provider
@Priority(Priorities.USER + 200)
@RequiresDeviceTreeContext
public class DevicesTreeContextProvider implements ContainerRequestFilter {
    @Override
    public void filter(ContainerRequestContext requestContext) {
        UriInfo uriInfo = requestContext.getUriInfo();

        List<Database> dbs = getDatabases(uriInfo);

        if(dbs.isEmpty()){
            requestContext.abortWith(
                    Response.ok(Collections.EMPTY_LIST, MediaType.APPLICATION_JSON).build());
            return;
        }


        List<String> filters = uriInfo.getQueryParameters(true).get("f");

        DeviceFilters df = new DeviceFilters(filters);

        DevicesTreeContext context = new DevicesTreeContext(dbs, df);


        ResteasyProviderFactory.pushContext(DevicesTreeContext.class, context);
    }

    private List<Database> getDatabases(UriInfo uriInfo) {
        List<PathSegment> segments = uriInfo.getPathSegments();

        if(segments.get(3).getPath().equalsIgnoreCase("tree")) {
            List<String> tango_hosts = uriInfo.getQueryParameters(true).get("v");
            if(tango_hosts == null) return Collections.emptyList();
            return tango_hosts.stream()
                    .filter(this::checkURISyntax)
                    .map(this::createDatabase)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
        } else {
            Optional<DatabaseDs> contextData = Optional.ofNullable(ResteasyProviderFactory.getContextData(DatabaseDs.class));
            return contextData.<List<Database>>map(databaseDs -> Lists.newArrayList(databaseDs.asDatabase())).orElse(Collections.emptyList());
        }
    }

    private Optional<Database> createDatabase(String s) {
        String[] host_port = s.split(":");
        return TangoDatabaseUtils.getDatabase(host_port[0], host_port[1]);
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
