package org.tango.rest;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.Database;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tango.rest.entities.TangoAlias;
import org.tango.rest.entities.TangoContainer;
import org.tango.rest.entities.TangoMember;
import org.tango.utils.DevFailedUtils;
import org.tango.web.server.tree.DeviceFilters;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
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

    private final List<Database> hosts;
    private final DeviceFilters filter;

    public DevicesTree(List<Database> hosts, DeviceFilters filter) {
        this.hosts = hosts;
        this.filter = filter;
    }

    @GET
    public Response get(){
        List<TangoContainer<TangoContainer<Object>>> result = hosts.stream()
                .map(database -> processTangoHost(database, filter))
                .collect(Collectors.toList());

        return Response.status(Response.Status.OK)
                .cacheControl(CacheControl.valueOf("public,max-age=3,max-age-millis=3000"))
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(result)
                .build();
    }

    private TangoContainer<TangoContainer<Object>> processTangoHost(String host, Database db, DeviceFilters filter) {
        TangoContainer result = new TangoContainer();
        result.$css = "tango_host";
        List<TangoContainer<?>> data = new ArrayList<>();
        try {
            result.value = db.getFullTangoHost();
            data.add(processAliases(db, filter));
            data.addAll(processDomains(result.value, db, filter));
        } catch (DevFailed devFailed) {
            logger.warn("Failed to get aliases list for {} due to {}",host, DevFailedUtils.toString(devFailed));
        }

        result.data = data;
        return result;
    }

    private List<TangoContainer<TangoContainer<TangoMember>>> processDomains(String host, Database db, DeviceFilters filter) {
        final List<String> domains = filter.getDomains(host, db);
        return domains.stream().map((domain) -> {
            TangoContainer<TangoContainer<TangoMember>> tangoDomain = new TangoContainer<>();
            tangoDomain.value = domain;
            List<String> device_family = filter.getFamilies(host,db, domain);

            tangoDomain.data = device_family.stream().map((family) -> {
                List<String> device_member = filter.getMembers(host, db, domain, family);
                return new TangoDomain(family, device_member.stream()
                        .map(member -> new TangoMember(member, domain + "/" + family + "/" + member, host + "/" + domain + "/" + family + "/" + member));
            return domain;

            return new TangoDomain(domain, .toArray());

            }).toArray());
        }).collect(Collectors.toList());
    }


    private TangoContainer<TangoAlias> processAliases(Database db, DeviceFilters filter) throws DevFailed {
        final String[] aliases = db.get_device_alias_list("*");

        TangoContainer<TangoAlias> result = new TangoContainer<>();
        result.value = "aliases";
        result.$css = "aliases";
        result.data = Arrays.stream(aliases).
                map((String alias) ->
                {
                    try {
                        TangoAlias tangoAlias = new TangoAlias();
                        tangoAlias.value = alias;
                        tangoAlias.device_name = db.get_device_from_alias(alias);
                        return tangoAlias;
                    } catch (DevFailed devFailed) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .filter(filter::checkDevice)
                .collect(Collectors.toList());

        return result;
    }
}
