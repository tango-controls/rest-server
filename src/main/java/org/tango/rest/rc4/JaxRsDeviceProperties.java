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
import com.google.common.collect.Iterables;
import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DbDatum;
import org.tango.web.server.binding.DynamicValue;
import org.tango.web.server.binding.Partitionable;
import org.tango.web.server.proxy.TangoDeviceProxy;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11/26/18
 */
@Path("/properties")
@Produces(MediaType.APPLICATION_JSON)
public class JaxRsDeviceProperties {
    @Context TangoDeviceProxy proxy;

    @GET
    @Partitionable
    @DynamicValue
    public List<Object> deviceProperties() throws DevFailed {
        String[] propnames = proxy.getProxy().toDeviceProxy().get_property_list("*");
        if(propnames.length == 0) return Collections.emptyList();
        return Arrays.stream(proxy.getProxy().toDeviceProxy().get_property(propnames))
                .map(DeviceHelper::dbDatumToResponse)
                .collect(Collectors.toList());
    }

    @POST
    @DynamicValue
    public Object post(@Context HttpServletRequest request) throws DevFailed {
        return put(request);
    }

    @PUT
    @DynamicValue
    public Object put(@Context HttpServletRequest request) throws DevFailed {
        Map<String, String[]> parametersMap = new HashMap<>(request.getParameterMap());
        boolean async = parametersMap.remove("async") != null;

        DbDatum[] input = Iterables.toArray(Iterables.transform(parametersMap.entrySet(), new Function<Map.Entry<String, String[]>, DbDatum>() {
            @Override
            public DbDatum apply(Map.Entry<String, String[]> input) {
                return new DbDatum(input.getKey(), input.getValue());
            }
        }), DbDatum.class);

        proxy.getProxy().toDeviceProxy().put_property(input);

        if (async)
            return null;
        else return deviceProperties();
    }

    @Path("/{prop}")
    public JaxRsDeviceProperty deviceProperty(@Context ResourceContext rc) {
        return rc.getResource(JaxRsDeviceProperty.class);
    }
}
