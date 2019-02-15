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
import com.google.common.collect.Lists;
import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.esrf.TangoApi.AttributeInfoEx;
import fr.esrf.TangoApi.CommandInfo;
import org.tango.rest.rc4.entities.DeviceState;
import org.tango.rest.rc4.entities.NamedEntity;
import org.tango.web.server.binding.DynamicValue;
import org.tango.web.server.binding.Partitionable;
import org.tango.web.server.binding.StaticValue;
import org.tango.web.server.proxy.TangoDatabaseProxy;
import org.tango.web.server.proxy.TangoDeviceProxy;

import javax.ws.rs.*;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Arrays;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 8/6/16
 */
@Path("/{domain}/{family}/{member}")
@Produces("application/json")
public class JaxRsDevice {
    @PathParam("domain") String domain;
    @PathParam("family") String family;
    @PathParam("member") String member;
    @Context TangoDatabaseProxy databaseProxy;
    @Context TangoDeviceProxy deviceProxy;

    @GET
    @StaticValue
    public Object get(@Context UriInfo uriInfo) throws DevFailed {
        final String devname = domain + "/" + family + "/" + member;
        return DeviceHelper.deviceToResponse(devname, databaseProxy.asEsrfDatabase().get_device_info(devname), uriInfo.getAbsolutePath());
    }

    @GET
    @Partitionable
    @StaticValue
    @Path("/attributes")
    public Object deviceAttributes(@Context UriInfo uriInfo) throws Exception {
        final String href = uriInfo.getAbsolutePath().toString();

        return Lists.transform(
                Arrays.asList(deviceProxy.getProxy().toDeviceProxy().get_attribute_info_ex()), new Function<AttributeInfoEx, Object>() {
                    @Override
                    public Object apply(final AttributeInfoEx input) {
                        return DeviceHelper.attributeInfoExToResponse(input.name, href);
                    }
                });
    }

    @GET
    @Partitionable
    @DynamicValue
    @Path("/attributes/info")
    public fr.esrf.TangoApi.AttributeInfoEx[] deviceAttributeInfos(@QueryParam("attr") String[] attrs,
                                                                   @Context UriInfo uriInfo) throws DevFailed {
        return deviceProxy.getProxy().toDeviceProxy().get_attribute_info_ex(attrs);
    }

    @GET
    @Partitionable
    @DynamicValue
    @Path("/attributes/value")
    public fr.esrf.TangoApi.DeviceAttribute[] deviceAttributeValues(@QueryParam("attr") String[] attrs,
                                                                    @Context UriInfo uriInfo) throws DevFailed {
        return deviceProxy.getProxy().toDeviceProxy().read_attribute(attrs);
    }

    @PUT
    @Partitionable
    @DynamicValue
    @Path("/attributes/value")
    public fr.esrf.TangoApi.DeviceAttribute[] deviceAttributeValuesPut(@Context UriInfo uriInfo) throws Exception {
        boolean async = uriInfo.getQueryParameters().containsKey("async");
        return DeviceHelper.getDeviceAttributesValue(deviceProxy, uriInfo.getQueryParameters().entrySet(),async);
    }

    @Path("/attributes/{attr}")
    public DeviceAttribute deviceAttribute(@Context ResourceContext rc) {
        return rc.getResource(DeviceAttribute.class);
    }

    @GET
    @DynamicValue
    @Path("/state")
    public DeviceState deviceState(@Context UriInfo uriInfo) {
        try {
            final String href = uriInfo.getAbsolutePath().resolve("..").toString();
            final fr.esrf.TangoApi.DeviceAttribute[] ss = deviceProxy.getProxy().toDeviceProxy().read_attribute(new String[]{"State", "Status"});
            DeviceState result = new DeviceState(ss[0].extractDevState().toString(), ss[1].extractString(), new Object() {
                public String _state = href + "attributes/State";
                public String _status = href + "attributes/Status";
                public String _parent = href;
                public String _self = href + "state";
            });

            return result;
        } catch (DevFailed devFailed) {
            return new DeviceState(DevState.UNKNOWN.toString(), String.format("Failed to read state&status from %s", deviceProxy.getName()));
        }
    }

    @GET
    @Partitionable
    @StaticValue
    @Path("/commands")
    public Object deviceCommands(@Context UriInfo uriInfo) throws DevFailed {
        final String href = uriInfo.getAbsolutePath().toString();
        return Lists.transform(Arrays.asList(deviceProxy.getProxy().toDeviceProxy().command_list_query()), new Function<CommandInfo, Object>() {
            @Override
            public Object apply(final CommandInfo input) {
                return DeviceHelper.commandInfoToResponse(input, href);
            }
        });
    }


    @Path("/commands/{cmd}")
    public DeviceCommand deviceCommand(@Context ResourceContext rc) {
        return rc.getResource(DeviceCommand.class);
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
        final URI href = uriInfo.getAbsolutePath();
        return Lists.transform(deviceProxy.getProxy().toDeviceProxy().getPipeNames(), new Function<String, Object>() {
            @Override
            public Object apply(final String input) {
                return new NamedEntity(input, href + "/" + input);
            }
        });
    }

    @Path("/pipes/{pipe}")
    public DevicePipe getPipe(@Context ResourceContext rc){
        return rc.getResource(DevicePipe.class);
    }
}
