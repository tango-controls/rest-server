package org.tango.rest;

import fr.esrf.Tango.DevFailed;
import fr.soleil.tango.clientapi.TangoAttribute;
import org.tango.rest.entities.Attribute;
import org.tango.rest.entities.AttributeValue;
import org.tango.rest.entities.Failures;
import org.tango.web.server.proxy.TangoAttributeProxy;
import org.tango.web.server.proxy.TangoAttributeProxyImpl;
import org.tango.web.server.util.AttributeUtils;
import org.tango.web.server.binding.*;
import org.tango.web.server.util.TangoSelector;

import javax.swing.text.html.Option;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11/16/18
 */
@Produces(MediaType.APPLICATION_JSON)
public class Attributes {

    @GET
    @Partitionable
    @StaticValue
    @RequiresTangoSelector
    public List<Attribute> get(@Context TangoSelector tangoSelector, final @Context UriInfo uriInfo){
        return tangoSelector.selectAttributes().stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(TangoAttributeProxyImpl::new)
                .map(tangoAttribute -> AttributeUtils.fromTangoAttribute(tangoAttribute, uriInfo)).collect(Collectors.toList());
    }

    @GET
    @Partitionable
    @DynamicValue
    @RequiresTangoSelector
    @Path("/value")
    public List<Object> read(@Context TangoSelector tangoSelector, final @Context UriInfo uriInfo){
        return tangoSelector.selectAttributes().stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(TangoAttributeProxyImpl::new)
                .map(tangoAttribute -> AttributeUtils.fromTangoAttribute(tangoAttribute, uriInfo))
                .map(AttributeUtils::getValueFromTangoAttribute)
                .collect(Collectors.toList());
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Partitionable
    @DynamicValue
    @Path("/value")
    public List<AttributeValue<?>> write(List<AttributeValue<?>> values){
        return values.stream()
                .map(AttributeUtils::setValueToTangoAttribute).collect(Collectors.toList());
    }

}
