package org.tango.rest;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DeviceData;
import fr.esrf.TangoApi.DeviceDataHistory;
import org.tango.client.ez.data.TangoDataWrapper;
import org.tango.client.ez.data.type.TangoDataType;
import org.tango.client.ez.data.type.TangoDataTypes;
import org.tango.client.ez.data.type.UnknownTangoDataType;
import org.tango.client.ez.data.type.ValueExtractionException;
import org.tango.client.ez.proxy.TangoProxy;
import org.tango.rest.entities.CommandResult;
import org.tango.rest.response.Responses;
import org.tango.web.server.command.CommandInput;
import org.tango.web.server.providers.Partitionable;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.util.Arrays;

/**
 * @author ingvord
 * @since 8/7/16
 */
@Path("/commands/{cmd}")
@Produces("application/json")
public class DeviceCommand {
    private final String name;
    private final TangoProxy proxy;


    public DeviceCommand(TangoProxy proxy, String name) {
        this.name = name;
        this.proxy = proxy;
    }

    @GET
    public Object get(@PathParam("cmd") String cmdName,
                      @Context TangoProxy proxy,
                      @Context UriInfo uriInfo) throws DevFailed {
        return DeviceHelper.commandInfoToResponse(proxy.toDeviceProxy().command_query(cmdName), uriInfo.getAbsolutePath().toString());
    }

    @PUT
    public Object deviceCommandPut(@PathParam("cmd") final String cmdName,
                                   @QueryParam("async") boolean async,
                                   @Context TangoProxy proxy,
                                   @Context UriInfo uriInfo,
                                   CommandInput value) throws Exception {
        final String href = uriInfo.getAbsolutePath().toString();

        if (async) {
            DeviceData data = new DeviceData();

            ((TangoDataType<Object>) TangoDataTypes.forClass(value.type)).insert(TangoDataWrapper.create(data), value.input);

            proxy.toDeviceProxy().command_inout_asynch(cmdName, data);
            return null;
        } else {
            final Object result = proxy.executeCommand(cmdName, value.input);
            return new Object() {
                public String name = cmdName;
                public Object output = result;
                public Object _links = new Object() {
                    public String _self = href;
                };
            };
        }
    }


    @GET
    @Partitionable
    @Path("/history")
    public Object deviceCommandHistory(@PathParam("cmd") String cmdName, @Context TangoProxy proxy, @Context UriInfo uriInfo) throws DevFailed {
        return Lists.transform(
                Arrays.asList(proxy.toDeviceProxy().command_history(cmdName)), new Function<DeviceDataHistory, Object>() {
                    @Override
                    public Object apply(DeviceDataHistory input) {
                        if (!input.hasFailed()) {
                            try {
                                TangoDataWrapper wrapper = TangoDataWrapper.create(input);

                                TangoDataType type = TangoDataTypes.forTangoDevDataType(input.getType());

                                CommandResult<Object, Object> result = new CommandResult<>();

                                result.name = input.getName();
                                result.input = null;
                                result.output = type.extract(wrapper);


                                return result;
                            } catch (UnknownTangoDataType | DevFailed | ValueExtractionException e) {
                                return Responses.createFailureResult(e);
                            }
                        } else {
                            return Responses.createFailureResult(new DevFailed(input.getErrStack()));
                        }
                    }
                });
    }


}
