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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tango.web.server.binding.RequiresDeviceTreeContext;
import org.tango.web.server.binding.StaticValue;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 2/14/17
 */
@Path("/rc5")
@Produces("application/json")
public class V10ApiImpl {
    private final Logger logger = LoggerFactory.getLogger(V10ApiImpl.class);

    @GET
    public Response authentication(@Context UriInfo uriInfo) {
        Map<String, String> result = new HashMap<>();

        result.put("hosts", uriInfo.getAbsolutePath() + "/hosts");

        Response.ResponseBuilder responseBuilder = Response.ok(result)
                .header("WWW-Authenticate", "Basic realm='Tango-Controls Realm'");

        return responseBuilder.build();
    }

    @GET
    @Path("/hosts")
    public void getHost() {
        throw new AssertionError("May not happen due to TangoDatabaseProvider");
    }

    @StaticValue
    @Path("/hosts/{host}")
    public JaxRsTangoHost getHost(@Context ResourceContext rc) {
        return rc.getResource(JaxRsTangoHost.class);
    }


    @Path("/devices/tree")
    @RequiresDeviceTreeContext
    public DevicesTree getDevicesTree(@Context ResourceContext rc) {
        return rc.getResource(DevicesTree.class);
    }

    @Path("/attributes")
    public JaxRsTangoAttributes getAttributes() {
        return new JaxRsTangoAttributes();
    }

    @Path("/commands")
    public JaxRsTangoCommands getCommands(@Context ResourceContext rc) {
        return rc.getResource(JaxRsTangoCommands.class);
    }

    @Path("/pipes")
    public JaxRsTangoPipes getPipes(@Context ResourceContext rc) {
        return rc.getResource(JaxRsTangoPipes.class);
    }
}
