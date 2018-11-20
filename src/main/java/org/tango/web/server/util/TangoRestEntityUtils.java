package org.tango.web.server.util;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.CommandInfo;
import fr.soleil.tango.clientapi.TangoAttribute;
import fr.soleil.tango.clientapi.TangoCommand;
import org.tango.rest.entities.*;
import org.tango.rest.entities.pipe.Pipe;
import org.tango.web.server.proxy.TangoAttributeProxy;
import org.tango.web.server.proxy.TangoPipeProxy;
import org.tango.web.server.response.TangoRestAttribute;
import org.tango.web.server.response.TangoRestCommand;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.StringJoiner;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11/17/18
 */
public class TangoRestEntityUtils {
    private TangoRestEntityUtils(){}

    public static TangoRestAttribute fromTangoAttribute(TangoAttributeProxy tangoAttribute, UriInfo uriInfo) {
        try {
            String host = tangoAttribute.getAttributeProxy().getDeviceProxy().get_tango_host();
            String device = tangoAttribute.getAttributeProxy().getDeviceProxy().get_name();
            String name = tangoAttribute.getDeviceAttribute().getName();

            URI href = getDeviceURI(uriInfo, host, device)
                    .path("attributes")
                    .path(name).build();

            return new TangoRestAttribute(
                    host, device, name, tangoAttribute.getAttributeProxy().get_info_ex(), href, tangoAttribute
            );
        } catch (DevFailed devFailed) {
            return new TangoRestAttribute(devFailed.errors);
        }
    }

    public static Object getValueFromTangoAttribute(TangoRestAttribute tangoRestAttribute) {
        try {
            tangoRestAttribute.attribute.update();

            return new AttributeValue<>(tangoRestAttribute.name, tangoRestAttribute.host, tangoRestAttribute.device, tangoRestAttribute.attribute.extract(), tangoRestAttribute.attribute.getQuality().toString(), tangoRestAttribute.attribute.getTimestamp());
        } catch (DevFailed devFailed) {
            return Failures.createInstance(devFailed);
        }

    }

    public static AttributeValue<?> setValueToTangoAttribute(AttributeValue<?> attributeValue) {
        StringJoiner stringJoiner = new StringJoiner("/");
        stringJoiner.add("tango:/").add(attributeValue.host).add(attributeValue.device).add(attributeValue.name);
        try {
            TangoAttribute tangoAttribute = new TangoAttribute(stringJoiner.toString());//TODO cache
            tangoAttribute.write(attributeValue.value);
            attributeValue.quality = tangoAttribute.getQuality().toString();
            attributeValue.timestamp = tangoAttribute.getTimestamp();
        } catch (DevFailed devFailed) {
            attributeValue.errors = devFailed.errors;
        }
        return attributeValue;
    }

    public static TangoAttribute newTangoAttribute(String path) {
        try {
            return new TangoAttribute(path);
        } catch (DevFailed devFailed) {
            try {
                return new TangoAttribute(path, null);
            } catch (DevFailed devFailed1) {
                throw new AssertionError(devFailed);
            }
        }
    }

    public static TangoRestCommand newTangoCommand(TangoCommand tangoCommand, UriInfo uriInfo) {
        try {
            String host = tangoCommand.getDeviceProxy().get_tango_host();
            String device = tangoCommand.getDeviceProxy().name();
            String name = tangoCommand.getCommandName();
            CommandInfo info = tangoCommand.getDeviceProxy().command_query(name);

            URI href = getDeviceURI(uriInfo, host, device).path("commands").path(name).build();

            return new TangoRestCommand(name, device, host, info, href, tangoCommand);
        } catch (DevFailed devFailed) {
            return new TangoRestCommand(devFailed.errors);
        }
    }

    private static UriBuilder getDeviceURI(UriInfo uriInfo, String host, String device) {
        return UriBuilder.fromUri(uriInfo.getBaseUri())
                .path("rest/rc5/hosts").path(host.replace(":",";port=")).path("devices").path(device);
    }

    public static CommandInOut<Object, Object> executeCommand(CommandInOut<Object, Object> input) {
        try {
            input.output = new TangoCommand("tango://" + input.host + "/" + input.device, input.name).executeExtract(input.input);
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
}
