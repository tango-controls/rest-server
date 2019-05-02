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

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tango.TangoRestServer;
import org.tango.client.ez.proxy.TangoProxyException;
import org.tango.rest.v10.DevicesTree;
import org.tango.web.server.binding.Partitionable;
import org.tango.web.server.binding.StaticValue;
import org.tango.web.server.proxy.TangoDatabaseProxy;
import org.tango.web.server.tree.DeviceFilters;
import org.tango.web.server.tree.DevicesTreeContextImpl;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.tango.web.server.providers.DevicesTreeContextProvider.WILDCARD;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 2/14/17
 */
@Path("/rc4")
@Produces("application/json")
public class Rc4ApiImpl {
    public static final String RC_4 = "rc4";
    private final Logger logger = LoggerFactory.getLogger(Rc4ApiImpl.class);

    @GET
    public Map<String, String> authentication(@Context UriInfo uriInfo, @Context ServletContext context, @Context TangoRestServer tangoContext) {
        Map<String, String> result = new HashMap<>();

        result.put("hosts", uriInfo.getAbsolutePath() + "/hosts");
        result.put("x-auth-method", "basic");

        return result;
    }

    @GET
    @Path("/hosts")
    public List<Map.Entry<String, String>> getHosts(@Context final UriInfo uriInfo, @Context TangoRestServer tangoContext) throws TangoProxyException {
        return Lists.newArrayList(Iterables.transform(tangoContext.getContext().hosts.asMap().values(), input -> new AbstractMap.SimpleEntry<String, String>(
                input.get().getTangoHost(),
                String.format("%s%s/%s", uriInfo.getAbsolutePath(), input.get().getHost(), input.get().getPort()))));
    }

    @GET
    @Partitionable
    @StaticValue
    @Path("/hosts/{host}/{port}")
    public Object getHost(@Context final UriInfo uriInfo,
                          @Context final TangoDatabaseProxy db,
                          @Context final ServletContext context) throws Exception {
        return new Object() {
            public String name = db.getName();
            public String host = db.getHost();
            public String port = db.getPort();
            public String[] info = db.getInfo();
            public String devices = uriInfo.getAbsolutePath() + "/devices";
        };
    }

    @Path("/hosts/{host}/{port}/devices")
    public Object get() {
        return new Devices();
    }

    @Path("/hosts/{host}/{port}/devices/{domain}/{family}/{member}")
    public JaxRsDevice getDevice(@Context ResourceContext rc) {
        return rc.getResource(JaxRsDevice.class);
    }

    @Path("/hosts/{host}/{port}/devices/tree")
    public DevicesTree getDevicesTreeSingleHost(@Context UriInfo uriInfo, @Context TangoDatabaseProxy db) {
        List<String> filters = uriInfo.getQueryParameters(true).get(WILDCARD);

        DeviceFilters df = new DeviceFilters(filters);

        DevicesTreeContextImpl context = new DevicesTreeContextImpl(Lists.newArrayList(db), df);
        return new DevicesTree(context);
    }

    @Path("/devices/tree")
    public DevicesTree getDevicesTree(@Context ResourceContext rc) {
        return rc.getResource(DevicesTree.class);
    }
}
