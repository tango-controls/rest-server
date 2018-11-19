package org.tango.web.server.response;

import fr.esrf.Tango.DevError;
import fr.esrf.TangoApi.AttributeInfoEx;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.tango.rest.entities.Attribute;
import org.tango.web.server.proxy.TangoAttributeProxy;

import java.net.URI;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11/17/18
 */
public class TangoRestAttribute extends Attribute{
    public AttributeInfoEx info;
    @JsonIgnore
    public TangoAttributeProxy attribute;

    public TangoRestAttribute(String host, String device, String name, AttributeInfoEx info, URI href, TangoAttributeProxy attribute) {
        super(host, device, name, null, href);
        this.info = info;
        this.attribute = attribute;
    }

    public TangoRestAttribute(DevError[] errors) {
        super();
        this.errors = errors;
    }
}
