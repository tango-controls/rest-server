package org.tango.rest;

import org.tango.rest.entities.Attribute;
import org.tango.web.server.util.AttributeUtils;
import org.tango.web.server.binding.*;
import org.tango.web.server.util.TangoSelector;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
                .map(tangoAttribute -> AttributeUtils.fromTangoAttribute(tangoAttribute, uriInfo)).collect(Collectors.toList());
    }

    @GET
    @Partitionable
    @DynamicValue
    @Path("/value")
    public List<Attribute> read(){
        return Collections.emptyList();
    }

    @PUT
    @Partitionable
    @DynamicValue
    @Path("/value")
    public List<Attribute> write(){
        return Collections.emptyList();
    }

}
