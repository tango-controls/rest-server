package org.tango.rest;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.Database;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tango.client.database.DatabaseFactory;
import org.tango.utils.DevFailedUtils;
import org.tango.rest.entities.DeviceFilters;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11/7/18
 */
@Path("/tree")
@Produces("application/json")
public class DevicesTree {
    private final Logger logger = LoggerFactory.getLogger(DevicesTree.class);

    private final Iterable<String> hosts;
    private final DeviceFilters filter;

    public DevicesTree(Iterable<String> hosts, DeviceFilters filter) {
        this.hosts = hosts;
        this.filter = filter;
    }

    @GET
    public Response get(){
        Iterator<String> it = hosts.iterator();
        Collection<TangoHost> result = new ArrayList<>();
        for(;it.hasNext();){
            String next = it.next();
            URI uri = null;
            try {
                uri = checkURISyntax(next);
            } catch (URISyntaxException e) {
                it.remove();
                continue;
            }

            Database db;
            try {
                Object obj = DatabaseFactory.getDatabase(uri.getHost(), String.valueOf(uri.getPort()));
                Field fldDatabase = obj.getClass().getDeclaredField("database");
                fldDatabase.setAccessible(true);
                db = (Database) fldDatabase.get(obj);
            } catch (DevFailed |IllegalAccessException|NoSuchFieldException devFailed) {
                logger.warn("Failed to get database for {}", next);
                it.remove();
                continue;
            }

            TangoHost tangoHost = processTangoHost(next, db, filter);
            result.add(tangoHost);
        }

        return Response.status(Response.Status.OK)
                .cacheControl(CacheControl.valueOf("public,max-age=3,max-age-millis=3000"))
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(result)
                .build();
    }

    private TangoHost processTangoHost(String host, Database db, DeviceFilters filter) {
        TangoHost result = new TangoHost(host, host);
        Collection data = new ArrayList();
        try {
            data.add(processAliases(host, db, filter));
        } catch (DevFailed devFailed) {
            logger.warn("Failed to get aliases list for {} due to {}",host, DevFailedUtils.toString(devFailed));
        }
        data.addAll(processDomains(host, db, filter));
        result.data = data.toArray();
        return result;
    }

    private Collection<Object> processDomains(String host, Database db, DeviceFilters filter) {
        final List<String> domains = filter.getDomains(host, db);
        return domains.stream().map((domain) -> {
            List<String> device_family = filter.getFamilies(host,db, domain);
            return new TangoDomain(domain, device_family.stream().map((family) -> {
                List<String> device_member = filter.getMembers(host, db, domain, family);
                return new TangoDomain(family, device_member.stream()
                        .map(member -> new TangoMember(member, domain + "/" + family + "/" + member, host + "/" + domain + "/" + family + "/" + member)).toArray());

            }).toArray());
        }).collect(Collectors.toList());
    }


    private Object processAliases(String host, Database db, DeviceFilters filter) throws DevFailed {
        final String[] aliases = db.get_device_alias_list("*");
        return new TangoAliases(
                Arrays.stream(aliases).
                        map((String alias) ->
                        {
                            try {
                                return new TangoAlias(alias, db.get_device_from_alias(alias));
                            } catch (DevFailed devFailed) {
                                return null;
                            }
                        }).
                        filter(Objects::nonNull)
                        .filter(filter::checkDevice)
                        .toArray());
    }

    private static class TangoAliases {
        public final String value = "aliases";
        public String $css = "aliases";
        public Object[] data;

        public TangoAliases(Object[] data) {
            this.data = data;
        }
    }

    private static class TangoDomain {
        public String value;
        public Object[] data;

        public TangoDomain(String value, Object[] data) {
            this.value = value;
            this.data = data;
        }
    }


    private URI checkURISyntax(String next) throws URISyntaxException{
        try {
            return new URI("tango://" + next);
        } catch (URISyntaxException e) {
            logger.warn("Provided Tango host[{}] has wrong URI syntax. Skipping...", next);
            throw e;
        }
    }

    private static class TangoHost {
        public String id;
        public String $css = "tango_host";
        public String value;
        public Object[] data;

        public TangoHost(String id, String value) {
            this.id = id;
            this.value = value;
        }
    }

    public static class TangoAlias{
        public String value;
        public String $css = "member";
        public boolean isAlias = true;
        public String device_name;

        public TangoAlias(String value, String device_name) {
            this.value = value;
            this.device_name = device_name;
        }
    }

    private static class TangoMember{
        public String value;
        public String $css = "member";
        public boolean isMember = true;
        public String device_name;
        public String device_id;

        public TangoMember(String value, String device_name, String device_id) {
            this.value = value;
            this.device_name = device_name;
            this.device_id = device_id;
        }
    }
}
