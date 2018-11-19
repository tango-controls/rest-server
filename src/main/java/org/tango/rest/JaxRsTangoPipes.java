package org.tango.rest;

import org.tango.rest.entities.Pipe;
import org.tango.web.server.binding.RequiresTangoSelector;
import org.tango.web.server.binding.StaticValue;
import org.tango.web.server.util.TangoRestEntityUtils;
import org.tango.web.server.util.TangoSelector;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author ingvord
 * @since 11/19/18
 */
@Path("/pipes")
@Produces(MediaType.APPLICATION_JSON)
public class JaxRsTangoPipes {

    @GET
    @RequiresTangoSelector
    @StaticValue
    public List<Pipe> get(@Context TangoSelector selector, @Context UriInfo uriInfo){
        return selector.selectPipesStream()
                .map(tangoPipeProxy -> TangoRestEntityUtils.newPipe(tangoPipeProxy, uriInfo))
                .collect(Collectors.toList());
    }

}
