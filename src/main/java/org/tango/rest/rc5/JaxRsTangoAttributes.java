package org.tango.rest.rc5;

import org.tango.rest.rc5.entities.Attribute;
import org.tango.rest.rc5.entities.AttributeValue;
import org.tango.web.server.util.TangoRestEntityUtils;
import org.tango.web.server.binding.*;
import org.tango.web.server.util.TangoSelector;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11/16/18
 */
@Produces(MediaType.APPLICATION_JSON)
public class JaxRsTangoAttributes {

    @GET
    @Partitionable
    @StaticValue
    @RequiresTangoSelector
    public List<Attribute> get(@Context TangoSelector tangoSelector, final @Context UriInfo uriInfo){
        return tangoSelector.selectAttributesStream()
                .map(tangoAttribute -> TangoRestEntityUtils.fromTangoAttribute(tangoAttribute, uriInfo))
                .collect(Collectors.toList());
    }

    @GET
    @Partitionable
    @DynamicValue
    @RequiresTangoSelector
    @Path("/value")
    public List<Object> read(@Context TangoSelector tangoSelector, final @Context UriInfo uriInfo){
        return tangoSelector.selectAttributesStream()
                .map(tangoAttribute -> TangoRestEntityUtils.fromTangoAttribute(tangoAttribute, uriInfo))
                .map(TangoRestEntityUtils::getValueFromTangoAttribute)
                .collect(Collectors.toList());
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Partitionable
    @DynamicValue
    @Path("/value")
    public List<AttributeValue<?>> write(@DefaultValue("false") @QueryParam("async") boolean async, List<AttributeValue<?>> values){
        if(async) {
            //TODO servlet async
            CompletableFuture.runAsync(() -> values.forEach(TangoRestEntityUtils::setValueToTangoAttribute));
            return null;
        } else {
            return values.stream()
                    .map(TangoRestEntityUtils::setValueToTangoAttribute).collect(Collectors.toList());
        }
    }

}
