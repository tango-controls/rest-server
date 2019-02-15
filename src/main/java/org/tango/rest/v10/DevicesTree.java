/*
 * Copyright 2019 Tango Controls
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tango.rest.v10;

import fr.esrf.Tango.DevFailed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tango.rest.v10.tree.*;
import org.tango.web.server.binding.RequiresDeviceTreeContext;
import org.tango.web.server.proxy.TangoDatabaseProxy;
import org.tango.web.server.tree.DeviceFilters;
import org.tango.web.server.tree.DevicesTreeContext;
import org.tango.web.server.tree.DevicesTreeContextImpl;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11/7/18
 */
@Path("/tree")
@Produces("application/json")
public class DevicesTree {
    private final Logger logger = LoggerFactory.getLogger(DevicesTree.class);

    @Context DevicesTreeContext context;

    public DevicesTree() {
    }

    public DevicesTree(DevicesTreeContextImpl context) {
        this.context = context;
    }

    @GET
    @RequiresDeviceTreeContext
    public Response get(){
        List<TangoHost> result = context.getHosts().stream()
                .map(database -> processTangoHost(database, context.getWildcards()))
                .collect(Collectors.toList());

        return Response.status(Response.Status.OK)
                .cacheControl(CacheControl.valueOf("public,max-age=3,max-age-millis=3000"))
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(result)
                .build();
    }

    private TangoHost processTangoHost(TangoDatabaseProxy db, DeviceFilters filter) {
        TangoHost result = new TangoHost();
        try {
            List<TangoContainer<?>> data = new ArrayList<>();
            result.id = db.getTangoHost();
            result.value = db.getTangoHost();
            data.add(processAliases(db, filter));
            data.addAll(processDomains(result.value, db, filter));
            result.data.addAll(data);
            return result;
        } catch (DevFailed devFailed) {
            result.isAlive = false;
            result.devFailed = devFailed;
            return result;
        }
    }

    private List<TangoDomain> processDomains(String host, TangoDatabaseProxy db, DeviceFilters filter) {
        final List<String> domains = filter.getDomains(host, db);
        return domains.stream().map((domain) -> {
            TangoDomain tangoDomain = new TangoDomain();
            tangoDomain.value = domain;
            List<String> device_family = filter.getFamilies(host,db, domain);

            tangoDomain.data.addAll(device_family.stream().map(getStringToTangoFamilyFunction(host, db, filter, domain)).collect(Collectors.toList()));
            return tangoDomain;
        }).collect(Collectors.toList());
    }

    private Function<String, TangoFamily> getStringToTangoFamilyFunction(String host, TangoDatabaseProxy db, DeviceFilters filter, String domain) {
        return (family) -> {
            TangoFamily tangoFamily = new TangoFamily();
            tangoFamily.value = family;
            List<String> members = filter.getMembers(host, db, domain, family);
            tangoFamily.data.addAll(members.stream()
                    .map(member -> new TangoMember(host + "/" + domain + "/" + family + "/" + member, member, domain + "/" + family + "/" + member)).collect(Collectors.toList()));
            return tangoFamily;
        };
    }


    private TangoContainer<TangoAlias> processAliases(TangoDatabaseProxy db, DeviceFilters filter) throws DevFailed {
        final String[] aliases = db.asEsrfDatabase().get_device_alias_list("*");

        TangoContainer<TangoAlias> result = new TangoContainer<>();
        result.value = "aliases";
        result.$css = "aliases";
        result.data.addAll(Arrays.stream(aliases).
                map((String alias) ->
                {
                    try {
                        TangoAlias tangoAlias = new TangoAlias();
                        tangoAlias.value = alias;
                        tangoAlias.device_name = db.asEsrfDatabase().get_device_from_alias(alias);
                        return tangoAlias;
                    } catch (DevFailed devFailed) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .filter(filter::checkDevice)
                .collect(Collectors.toList()));

        return result;
    }
}
