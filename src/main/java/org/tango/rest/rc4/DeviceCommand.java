package org.tango.rest.rc4;

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
import org.tango.rest.rc4.entities.CommandResult;
import org.tango.rest.rc4.entities.Failures;
import org.tango.web.server.binding.Partitionable;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
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
                      @Context UriInfo uriInfo) throws DevFailed {
        return DeviceHelper.commandInfoToResponse(proxy.toDeviceProxy().command_query(cmdName), uriInfo.getAbsolutePath().toString());
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Object deviceCommandPut(@QueryParam("async") boolean async,
                                   @Context UriInfo uriInfo,
                                   CommandInput value) throws Exception {
        if (async) {
            DeviceData data = new DeviceData();

            ((TangoDataType<Object>) TangoDataTypes.forClass(value.type)).insert(TangoDataWrapper.create(data), value.input);

            proxy.toDeviceProxy().command_inout_asynch(name, data);
            return null;
        } else {
            final Object output = proxy.executeCommand(name, value.input);
            return new CommandResult<>(name, output);
        }
    }


    @GET
    @Partitionable
    @Path("/history")
    public Object deviceCommandHistory(@PathParam("cmd") String cmdName, @Context UriInfo uriInfo) throws DevFailed {
        return Lists.transform(
                Arrays.asList(proxy.toDeviceProxy().command_history(cmdName)), new Function<DeviceDataHistory, Object>() {
                    @Override
                    public Object apply(DeviceDataHistory input) {
                        if (!input.hasFailed()) {
                            try {
                                TangoDataWrapper wrapper = TangoDataWrapper.create(input);

                                TangoDataType type = TangoDataTypes.forTangoDevDataType(input.getType());

                                return new CommandResult<>(input.getName(), type.extract(wrapper));
                            } catch (UnknownTangoDataType | DevFailed | ValueExtractionException e) {
                                return Failures.createInstance(e);
                            }
                        } else {
                            return Failures.createInstance(new DevFailed(input.getErrStack()));
                        }
                    }
                });
    }


    /**
     * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
     * @since 8/8/16
     */
    public static class CommandInput {
        public String cmdName;
        public Class<?> type;
        public Object input;

        public CommandInput(String cmdName, Class<?> type, Object converted) {
            this.cmdName = cmdName;
            this.type = type;
            this.input = converted;
        }
    }
}
