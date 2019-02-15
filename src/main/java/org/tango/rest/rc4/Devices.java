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

package org.tango.rest.rc4;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import fr.esrf.Tango.DevFailed;
import org.tango.client.ez.proxy.TangoProxyException;
import org.tango.rest.rc4.entities.NamedEntity;
import org.tango.web.server.binding.Partitionable;
import org.tango.web.server.binding.StaticValue;
import org.tango.web.server.proxy.TangoDatabaseProxy;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.util.List;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 5/15/17
 */
@Path("/devices")
@Produces("application/json")
public class Devices {
    @GET
    @StaticValue
    @Partitionable
    public Object get(@DefaultValue("*/*/*") @QueryParam("wildcard") String wildcard,
                      @Context final UriInfo uriInfo,
                      @Context TangoDatabaseProxy db,
                      @Context final ServletContext context) throws TangoProxyException {
        List<String> result = db.getDeviceNames(wildcard);
        List<NamedEntity> transform = Lists.transform(result, new Function<String, NamedEntity>() {
            @Override
            public NamedEntity apply(final String input) {
                return new NamedEntity(input, uriInfo.getAbsolutePath() + "/" + input);
            }
        });
        return transform;
    }

    @GET
    @Path("/{domain}")
    @StaticValue
    @Partitionable
    public Object getFamilies(@PathParam("domain") String domain,
                              @DefaultValue("*") @QueryParam("wildcard") String wildcard,
                              @Context final UriInfo uriInfo,
                              @Context TangoDatabaseProxy db,
                              @Context final ServletContext context) throws DevFailed {
        return db.asEsrfDatabase().get_device_family(domain +"/"+ wildcard);
    }

    @GET
    @Path("/{domain}/{family}")
    @StaticValue
    @Partitionable
    public Object getMembers(@PathParam("domain") String domain,
                             @PathParam("family") String family,
                             @DefaultValue("*") @QueryParam("wildcard") String wildcard,
                             @Context final UriInfo uriInfo,
                             @Context TangoDatabaseProxy db,
                             @Context final ServletContext context) throws DevFailed {
        return db.asEsrfDatabase().get_device_member(domain + "/" + family + "/" + wildcard);
    }
}
