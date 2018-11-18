package org.tango.web.server.util;

import fr.esrf.Tango.DevFailed;
import fr.soleil.tango.clientapi.TangoAttribute;
import org.tango.rest.entities.AttributeValue;
import org.tango.rest.entities.Failures;
import org.tango.web.server.proxy.TangoAttributeProxy;
import org.tango.web.server.response.TangoRestAttribute;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.StringJoiner;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11/17/18
 */
public class AttributeUtils {
    private AttributeUtils(){}

    public static TangoRestAttribute fromTangoAttribute(TangoAttributeProxy tangoAttribute, UriInfo uriInfo) {
        try {
            String host = tangoAttribute.getAttributeProxy().getDeviceProxy().get_tango_host();
            String device = tangoAttribute.getAttributeProxy().getDeviceProxy().get_name();
            String name = tangoAttribute.getDeviceAttribute().getName();

            StringJoiner stringJoiner = new StringJoiner("/");
            String id = stringJoiner.add(host).add(device).add(name).toString();

            URI href = UriBuilder.fromUri(uriInfo.getBaseUri())
                    .path("rest/rc5/hosts")
                    .path(host.replace(":",";port="))
                    .path("devices")
                    .path(device)
                    .path("attributes")
                    .path(name).build();

            return new TangoRestAttribute(
                    id, name, device, host, tangoAttribute.getAttributeProxy().get_info_ex(), href, tangoAttribute
            );
        } catch (DevFailed devFailed) {
            throw new RuntimeException(devFailed);
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
            TangoAttribute tangoAttribute = new TangoAttribute(stringJoiner.toString());
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
}
