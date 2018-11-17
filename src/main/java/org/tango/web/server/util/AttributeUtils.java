package org.tango.web.server.util;

import fr.esrf.Tango.DevFailed;
import fr.soleil.tango.clientapi.TangoAttribute;
import org.tango.rest.entities.AttributeValue;
import org.tango.rest.entities.Failures;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.StringJoiner;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11/17/18
 */
public class AttributeUtils {
    private AttributeUtils(){}

    public static org.tango.web.server.response.TangoAttribute fromTangoAttribute(TangoAttribute tangoAttribute, UriInfo uriInfo) {
        try {
            org.tango.web.server.response.TangoAttribute attribute = new org.tango.web.server.response.TangoAttribute();

            attribute.name = tangoAttribute.getDeviceAttribute().getName();
            attribute.host = tangoAttribute.getAttributeProxy().getDeviceProxy().get_tango_host();
            attribute.device = tangoAttribute.getAttributeProxy().getDeviceProxy().get_name();
            attribute.info = tangoAttribute.getAttributeProxy().get_info_ex();
            StringJoiner stringJoiner = new StringJoiner("/");
            stringJoiner.add(attribute.host).add(attribute.device).add(attribute.name);
            attribute.id = stringJoiner.toString();

            String uri = UriBuilder.fromUri(uriInfo.getBaseUri())
                    .path("rest/rc5/hosts")
                    .path(attribute.host.replace(":",";port="))
                    .path("devices")
                    .path(attribute.device)
                    .path("attributes")
                    .path(attribute.name).build().toString();

            attribute.value = uri + "/value";
            attribute.history = uri + "/history";
            attribute.properties = uri + "/properties";

            attribute.attribute = tangoAttribute;
            
            return attribute;
        } catch (DevFailed devFailed) {
            throw new RuntimeException(devFailed);
        }
    }

    public static Object getValueFromTangoAttribute(org.tango.web.server.response.TangoAttribute tangoAttribute) {
        try {
            tangoAttribute.attribute.update();

            return new AttributeValue<>(tangoAttribute.name,tangoAttribute.host,tangoAttribute.device,tangoAttribute.attribute.extract(),tangoAttribute.attribute.getQuality().toString(),tangoAttribute.attribute.getTimestamp());
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
}
