package org.tango.web.server.response;

import fr.esrf.TangoApi.AttributeInfoEx;
import fr.soleil.tango.clientapi.TangoAttribute;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.tango.rest.entities.Attribute;

import java.net.URI;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11/17/18
 */
public class TangoRestAttribute extends Attribute{
    public AttributeInfoEx info;
    @JsonIgnore
    public TangoAttribute attribute;

    public TangoRestAttribute(String id, String name, String device, String host, AttributeInfoEx info, URI href, TangoAttribute attribute) {
        super(id, name, device, host, null, href);
        this.info = info;
        this.attribute = attribute;
    }
}
