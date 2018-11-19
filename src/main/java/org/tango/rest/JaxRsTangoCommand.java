package org.tango.rest;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DeviceDataHistory;
import org.tango.client.ez.data.TangoDataWrapper;
import org.tango.client.ez.data.type.TangoDataType;
import org.tango.client.ez.data.type.TangoDataTypes;
import org.tango.client.ez.data.type.UnknownTangoDataType;
import org.tango.client.ez.data.type.ValueExtractionException;
import org.tango.rest.entities.Command;
import org.tango.rest.entities.CommandInOut;
import org.tango.utils.DevFailedUtils;
import org.tango.web.server.binding.DynamicValue;
import org.tango.web.server.binding.Partitionable;
import org.tango.web.server.binding.RequiresTangoCommand;
import org.tango.web.server.proxy.TangoCommandProxy;
import org.tango.web.server.proxy.TangoDatabaseProxy;
import org.tango.web.server.proxy.TangoDeviceProxy;
import org.tango.web.server.util.TangoRestEntityUtils;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.Arrays;
import java.util.List;

/**
 * @author ingvord
 * @since 8/7/16
 */
@Path("/commands/{cmd}")
@Produces("application/json")
@RequiresTangoCommand
public class JaxRsTangoCommand {
    @PathParam("cmd") public String name;
    @Context public TangoDatabaseProxy database;
    @Context public TangoDeviceProxy proxy;
    @Context public TangoCommandProxy command;

    @GET
    public Command get(@Context UriInfo uriInfo) {
        return TangoRestEntityUtils.newTangoCommand(command.asTangoCommand(), uriInfo);
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public CommandInOut<Object, Object> put(@QueryParam("async") boolean async,
                      @Context UriInfo uriInfo,
                      CommandInOut<Object, Object> value) throws Exception {
        if (async) {
            command.execute(value.input);
            return null;
        } else {
            value.output = command.executeExtract(value.input);
            return value;
        }
    }

//    @PUT
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.TEXT_PLAIN)
//    public Object put(CommandInOut<Object, Object> value) throws Exception {
//        return command.executeExtract(value.input);
//    }

    @GET
    @DynamicValue
    @Partitionable
    @Path("/history")
    public List<CommandInOut<Object, Object>> deviceCommandHistory(@Context UriInfo uriInfo) throws DevFailed {
        return Lists.transform(
                Arrays.asList(proxy.getProxy().toDeviceProxy().command_history(name)), new Function<DeviceDataHistory, CommandInOut<Object, Object>>() {
                    @Override
                    public CommandInOut<Object, Object> apply(DeviceDataHistory input) {
                        CommandInOut<Object, Object> result = new CommandInOut<>();
                        result.name = name;
                        result.host = database.getTangoHost();
                        result.device = proxy.getName();
                        if (!input.hasFailed()) {
                            try {
                                TangoDataWrapper wrapper = TangoDataWrapper.create(input);

                                TangoDataType type = TangoDataTypes.forTangoDevDataType(input.getType());

                                result.output = type.extract(wrapper);
                            } catch (UnknownTangoDataType | ValueExtractionException e) {
                                result.errors = DevFailedUtils.buildDevError(e.getClass().getSimpleName(),e.getMessage(),0);
                            } catch (DevFailed devFailed) {
                                result.errors = devFailed.errors;
                            }
                        } else {
                            result.errors = input.getErrStack();
                        }
                        return result;
                    }
                });
    }


}
