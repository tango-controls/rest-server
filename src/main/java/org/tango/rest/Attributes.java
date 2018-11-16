package org.tango.rest;

import org.tango.rest.entities.Attribute;
import org.tango.web.server.binding.*;
import org.tango.web.server.util.TangoSelector;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import java.util.Collections;
import java.util.List;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11/16/18
 */
public class Attributes {

    @GET
    @Partitionable
    @StaticValue
    @RequiresTangoSelector
    public List<Attribute> get(@Context TangoSelector tangoSelector){
        return tangoSelector.selectAttributes();
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
