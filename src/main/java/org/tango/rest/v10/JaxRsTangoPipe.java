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
import fr.esrf.TangoApi.PipeBlob;
import org.tango.rest.v10.entities.pipe.Pipe;
import org.tango.rest.v10.entities.pipe.PipeValue;
import org.tango.web.server.binding.DynamicValue;
import org.tango.web.server.binding.RequiresTangoPipe;
import org.tango.web.server.proxy.TangoPipeProxy;
import org.tango.web.server.util.TangoRestEntityUtils;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.util.concurrent.CompletableFuture;

/**
 * @author ingvord
 * @since 8/7/16
 */
@Path("/pipes/{pipe}")
@Produces("application/json")
@RequiresTangoPipe
public class JaxRsTangoPipe {
    @PathParam("pipe") String name;
    @Context TangoPipeProxy proxy;

    @GET
    @DynamicValue
    public Pipe get(@Context UriInfo uriInfo) {
        return TangoRestEntityUtils.newPipe(proxy, uriInfo);
    }

    @GET
    @DynamicValue
    @Path("/value")
    public PipeValue getValue() {
        return TangoRestEntityUtils.newPipeValue(proxy);
    }

    @PUT
    @Consumes("application/json")
    @DynamicValue
    @Path("/value")
    public PipeValue devicePipePut(@QueryParam("async") boolean async, PipeBlob blob) throws DevFailed {
        if (async) {
            CompletableFuture.runAsync(() -> {
                try {
                    proxy.write(blob);
                } catch (DevFailed ignored) {
                }
            });
            return null;
        } else {
            proxy.write(blob);
            return getValue();
        }
    }
}
