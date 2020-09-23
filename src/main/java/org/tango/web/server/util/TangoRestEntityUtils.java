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

package org.tango.web.server.util;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.CommandInfo;
import fr.esrf.TangoApi.DevicePipe;
import org.javatuples.Pair;
import org.tango.client.ez.proxy.NoSuchAttributeException;
import org.tango.client.ez.proxy.ValueTimeQuality;
import org.tango.rest.entities.Failures;
import org.tango.rest.v10.entities.AttributeValue;
import org.tango.rest.v10.entities.CommandInOut;
import org.tango.rest.v10.entities.pipe.Pipe;
import org.tango.utils.DevFailedUtils;
import org.tango.web.server.proxy.TangoAttributeProxy;
import org.tango.web.server.proxy.TangoCommandProxy;
import org.tango.web.server.proxy.TangoPipeProxy;
import org.tango.web.server.response.TangoPipeValue;
import org.tango.web.server.response.TangoRestAttribute;
import org.tango.web.server.response.TangoRestCommand;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11/17/18
 */
public class TangoRestEntityUtils {
    private TangoRestEntityUtils(){}

    public static TangoRestAttribute fromTangoAttribute(TangoAttributeProxy tangoAttribute, UriInfo uriInfo) throws NoSuchAttributeException {
        try {
            String host = tangoAttribute.getHost();
            String device = tangoAttribute.getDevice();
            String name = tangoAttribute.getName();

            URI href = getDeviceURI(uriInfo, host, device)
                    .path("attributes")
                    .path(name).build();

            return new TangoRestAttribute(
                    host, device, name, tangoAttribute.getDeviceProxy().get_attribute_info_ex(name), href, tangoAttribute
            );
        } catch (DevFailed devFailed) {
            if (devFailed.errors.length > 0 && devFailed.errors[0].reason.equalsIgnoreCase("API_AttrNotFound"))
                throw new NoSuchAttributeException();
            return new TangoRestAttribute(devFailed.errors);
        }
    }

    public static Object getValueFromTangoAttribute(TangoRestAttribute tangoRestAttribute) {
        try {
            ValueTimeQuality<Object> result = tangoRestAttribute.attribute.read();

            return new AttributeValue<>(
                    tangoRestAttribute.name,
                    tangoRestAttribute.host,
                    tangoRestAttribute.device,
                    result.value,
                    result.quality.toString(),
                    result.time);
        } catch (DevFailed devFailed) {
            return Failures.createInstance(devFailed);
        }

    }

    public static AttributeValue<?> setValueToTangoAttribute(Pair<AttributeValue<?>, TangoAttributeProxy> pair) {
        AttributeValue<?> attributeValue = pair.getValue0();
        try {
            pair.getValue1().write(attributeValue.value);
            attributeValue.quality = "PENDING";
            attributeValue.timestamp = System.currentTimeMillis();
        } catch (Exception devFailed) {
            attributeValue.quality = "FAILURE";
            attributeValue.timestamp = System.currentTimeMillis();
            attributeValue.errors = devFailed.getClass().isAssignableFrom(DevFailed.class) ?
                    ((DevFailed) devFailed).errors :
                    DevFailedUtils.newDevFailed(devFailed).errors;
        }
        return attributeValue;
    }

    public static TangoRestCommand newTangoCommand(TangoCommandProxy tangoCommand, UriInfo uriInfo) {
        try {
            String host = tangoCommand.getDeviceProxy().get_tango_host();
            String device = tangoCommand.getDeviceProxy().name();
            String name = tangoCommand.getCommandName();
            CommandInfo info = tangoCommand.getDeviceProxy().command_query(name);

            URI href = getDeviceURI(uriInfo, host, device).path("commands").path(name).build();

            return new TangoRestCommand(name, device, host, info, href);
        } catch (DevFailed devFailed) {
            return new TangoRestCommand(devFailed.errors);
        }
    }

    private static UriBuilder getDeviceURI(UriInfo uriInfo, String host, String device) {
        return UriBuilder.fromUri(uriInfo.getBaseUri())
                .path("rest/v11/hosts").path(host.replace(":", ";port=")).path("devices").path(device);
    }

    public static CommandInOut<Object, Object> executeCommand(TangoCommandProxy command, CommandInOut<Object, Object> input) {
        try {
            input.output = command.executeExtract(input.input);
        } catch (DevFailed devFailed) {
            input.errors = devFailed.errors;
        }
        return input;

    }

    public static Pipe newPipe(TangoPipeProxy tangoPipeProxy, UriInfo uriInfo) {
        Pipe result = new Pipe();
        try{
            result.device = tangoPipeProxy.getDeviceName();
            result.name = tangoPipeProxy.getName();
            result.host = tangoPipeProxy.getTangoHost();
            result.info = tangoPipeProxy.getInfo();

            result.id = result.host + "/" + result.device + "/" + result.name;

            URI href = getDeviceURI(uriInfo, result.host, result.device).path("pipes").path(result.name).build();
            result.value = href + "/value";
        } catch (DevFailed devFailed) {
            result.errors = devFailed.errors;
        }
        return result;
    }

    public static TangoPipeValue newPipeValue(TangoPipeProxy proxy) {
        TangoPipeValue result = new TangoPipeValue();
        result.host = proxy.getTangoHost();
        result.device = proxy.getDeviceName();
        result.name = proxy.getName();

        try {
            DevicePipe devicePipe = proxy.read();
            result.timestamp = devicePipe.getTimeValMillisSec();
            result.data = devicePipe.getPipeBlob();
        } catch (DevFailed devFailed) {
            result.errors = devFailed.errors;
        }
        return result;
    }
}
