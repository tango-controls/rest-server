package org.tango.web.server.attribute;

import fr.esrf.TangoApi.AttributeInfoEx;
import org.tango.rest.entities.Attribute;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11/17/18
 */
public class TangoAttribute extends Attribute{
    public AttributeInfoEx info;
}
