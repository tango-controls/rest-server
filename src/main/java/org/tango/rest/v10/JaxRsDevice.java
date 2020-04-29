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

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.soleil.tango.clientapi.TangoAttribute;
import org.tango.rest.rc4.DeviceHelper;
import org.tango.rest.rc4.JaxRsDeviceProperties;
import org.tango.rest.v10.entities.*;
import org.tango.web.server.binding.DynamicValue;
import org.tango.web.server.binding.Partitionable;
import org.tango.web.server.binding.RequiresTangoAttribute;
import org.tango.web.server.binding.StaticValue;
import org.tango.web.server.proxy.TangoDatabaseProxy;
import org.tango.web.server.proxy.TangoDeviceProxy;
import org.tango.web.server.response.TangoRestAttribute;
import org.tango.web.server.response.TangoRestCommand;
import org.tango.web.server.response.TangoRestDevice;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 8/6/16
 */
@Path("/{domain}/{family}/{member}")
@Produces("application/json")
public class JaxRsDevice {
    @Context private TangoDatabaseProxy tangoDatabase;
    @Context private TangoDeviceProxy tangoDevice;

    @GET
    @StaticValue
    public Device get(@Context UriInfo uriInfo) throws DevFailed{
        return new TangoRestDevice(
                tangoDatabase.getTangoHost() + "/" + tangoDevice.getName(),
                tangoDevice.getName(),
                tangoDatabase.getTangoHost(),
                tangoDevice.getAlias(),
                tangoDatabase.asEsrfDatabase().get_device_info(tangoDevice.getName()), uriInfo.getAbsolutePath());
    }

    @GET
    @Partitionable
    @StaticValue
    @Path("/attributes")
    public List<Attribute> deviceAttributes(@Context UriInfo uriInfo) throws DevFailed {
        return Arrays.stream(tangoDevice.getProxy().toDeviceProxy().get_attribute_info_ex(tangoDevice.getProxy().toDeviceProxy().get_attribute_list()))
                .map(infoEx -> new TangoRestAttribute(
                        tangoDatabase.getTangoHost(),
                        tangoDevice.getName(),
                        infoEx.name,
                        infoEx,
                        uriInfo.getAbsolutePathBuilder().path(infoEx.name).build() ,
                        null))
                .collect(Collectors.toList());
    }

    @GET
    @Partitionable
    @DynamicValue
    @Path("/attributes/info")
    public fr.esrf.TangoApi.AttributeInfoEx[] deviceAttributeInfos(@QueryParam("attr") String[] attrs) throws DevFailed {
        return tangoDevice.getProxy().toDeviceProxy().get_attribute_info_ex(attrs);
    }

    @GET
    @Partitionable
    @DynamicValue
    @Path("/attributes/value")
    //TODO replace with AttributeValue
    public fr.esrf.TangoApi.DeviceAttribute[] deviceAttributeValues(@QueryParam("attr") String[] attrs) throws DevFailed {
        return tangoDevice.getProxy().toDeviceProxy().read_attribute(attrs);
    }

    @PUT
    @Partitionable
    @DynamicValue
    @Path("/attributes/value")
    //TODO replace with AttributeValue
    public fr.esrf.TangoApi.DeviceAttribute[] deviceAttributeValuesPut(@Context ServletContext context,
                                                                       @Context UriInfo uriInfo) throws Exception {
        boolean async = uriInfo.getQueryParameters().containsKey("async");
        return DeviceHelper.getDeviceAttributesValue(tangoDevice, uriInfo.getQueryParameters().entrySet(), async);
    }

    @Path("/attributes/{attr}")
    @RequiresTangoAttribute
    public JaxRsDeviceAttribute deviceAttribute(@Context
                                                        ResourceContext rc, @PathParam("attr") String attrName, @Context TangoAttribute tangoAttribute) throws Exception {
        return rc.getResource(JaxRsDeviceAttribute.class);
    }

    @GET
    @DynamicValue
    @Path("/state")
    public Object deviceState(@Context ServletContext context, @Context UriInfo uriInfo) {
        try {
            final fr.esrf.TangoApi.DeviceAttribute[] ss = tangoDevice.getProxy().toDeviceProxy().read_attribute(new String[]{"State", "Status"});
            DeviceState result = new DeviceState(ss[0].extractDevState().toString(), ss[1].extractString());

            return result;
        } catch (DevFailed devFailed) {
            return new DeviceState(DevState.UNKNOWN.toString(), String.format("Failed to read state&status from %s", tangoDevice.getName()));
        }
    }

    @GET
    @Partitionable
    @StaticValue
    @Path("/commands")
    public List<Command> deviceCommands(@Context UriInfo uriInfo) throws DevFailed {
        return Arrays.stream(tangoDevice.getProxy().toDeviceProxy().command_list_query())
                .map(commandInfo -> new TangoRestCommand(
                        commandInfo.cmd_name,
                        tangoDevice.getName(),
                        tangoDatabase.getTangoHost(),
                        commandInfo,
                        uriInfo.getAbsolutePathBuilder().path(commandInfo.cmd_name).build(),
                        null))
                .collect(Collectors.toList());
    }


    @Path("/commands/{cmd}")
    public JaxRsTangoCommand deviceCommand(@Context ResourceContext rc) {
        return rc.getResource(JaxRsTangoCommand.class);
    }

    @Path("/properties")
    public JaxRsDeviceProperties getProperties(@Context ResourceContext rc){
        return rc.getResource(JaxRsDeviceProperties.class);
    }

    @GET
    @Partitionable
    @StaticValue
    @Path("/pipes")
    public Object devicePipes(@Context UriInfo uriInfo) throws DevFailed {
        final UriBuilder href = uriInfo.getAbsolutePathBuilder();
        return Lists.transform(tangoDevice.getProxy().toDeviceProxy().getPipeNames(), new Function<String, Object>() {
            @Override
            public Object apply(final String input) {
                return new NamedEntity(input, null, href.path(input).build());
            }
        });
    }

    @Path("/pipes/{pipe}")
    public JaxRsTangoPipe getPipe(@Context ResourceContext rc){
        return rc.getResource(JaxRsTangoPipe.class);
    }
}
