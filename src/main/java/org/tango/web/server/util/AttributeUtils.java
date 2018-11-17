package org.tango.web.server.util;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.AttributeEventInfo;
import fr.esrf.TangoApi.AttributeInfoEx;
import fr.esrf.TangoDs.TangoConst;
import fr.soleil.tango.clientapi.TangoAttribute;
import org.tango.rest.entities.Attribute;
import org.tango.rest.entities.AttributeInfo;

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

    public static org.tango.web.server.attribute.TangoAttribute fromTangoAttribute(TangoAttribute tangoAttribute, UriInfo uriInfo) {
        try {
            org.tango.web.server.attribute.TangoAttribute attribute = new org.tango.web.server.attribute.TangoAttribute();

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
            
            return attribute;
        } catch (DevFailed devFailed) {
            throw new RuntimeException(devFailed);
        }
    }
}
