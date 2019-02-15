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

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DbDatum;
import org.tango.web.server.binding.DynamicValue;
import org.tango.web.server.proxy.TangoDeviceProxy;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;

/**
 * @author ingvord
 * @since 8/7/16
 */
@Path("/{prop}")
@Produces("application/json")
public class JaxRsDeviceProperty {
    @PathParam("prop") String name;
    @Context TangoDeviceProxy proxy;


    @GET
    @DynamicValue
    public Object get() throws DevFailed {
        return DeviceHelper.dbDatumToResponse(proxy.getProxy().toDeviceProxy().get_property(name));
    }

    @DELETE
    public void delete() throws DevFailed {
        proxy.getProxy().toDeviceProxy().delete_property(name);
    }

    @POST
    @DynamicValue
    public Object post(@FormParam("value") String[] value,
                       @QueryParam("async") boolean async) throws DevFailed {
        return put(value, async);
    }

    @PUT
    @DynamicValue
    public Object put(@QueryParam("value") String[] value,
                      @QueryParam("async") boolean async) throws DevFailed {
        DbDatum input = new DbDatum(name, value);

        proxy.getProxy().toDeviceProxy().put_property(input);

        if (async)
            return null;
        else return DeviceHelper.dbDatumToResponse(proxy.getProxy().toDeviceProxy().get_property(name));
    }
}
