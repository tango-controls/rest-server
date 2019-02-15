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

import org.tango.rest.v10.entities.Command;
import org.tango.rest.v10.entities.CommandInOut;
import org.tango.web.server.binding.DynamicValue;
import org.tango.web.server.binding.Partitionable;
import org.tango.web.server.binding.RequiresTangoSelector;
import org.tango.web.server.binding.StaticValue;
import org.tango.web.server.proxy.TangoCommandProxy;
import org.tango.web.server.util.TangoRestEntityUtils;
import org.tango.web.server.util.TangoSelector;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author ingvord
 * @since 11/18/18
 */
@Path("/commands")
@Produces(MediaType.APPLICATION_JSON)
public class JaxRsTangoCommands {

    @GET
    @StaticValue
    @Partitionable
    @RequiresTangoSelector
    public List<Command> get(@Context TangoSelector selector, @Context UriInfo uriInfo){
        return selector.selectCommandsStream()
                .map(TangoCommandProxy::asTangoCommand)
                .map(tangoCommand -> TangoRestEntityUtils.newTangoCommand(tangoCommand, uriInfo))
                .collect(Collectors.toList());
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @DynamicValue
    public List<CommandInOut<Object,Object>> execute(List<CommandInOut<Object,Object>> inputs){
        return inputs.stream()
                .map(TangoRestEntityUtils::executeCommand)
                .collect(Collectors.toList());
    }
}
