package org.tango.rest;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 27.11.2015
 */
@Path("/") //relative to rest
@Produces("application/json")
public class EntryPoint {
    private static final List<String> SUPPORTED_VERSIONS = new ArrayList<>(2);
    static {
        SUPPORTED_VERSIONS.add("rc1");
        SUPPORTED_VERSIONS.add("mtango");
    }

    private static final String SUPPORTED_AUTHENTICATION = "basic";

    @GET
    public Map<String,String> versions(@Context ServletContext context){
        Map<String,String> result = new HashMap<>();

        for(String version : SUPPORTED_VERSIONS){
            result.put(version, context.getContextPath() + "/rest/" + version);
        }

        return result;
    }

    @GET
    @Path("/{version}")
    public Map<String, String> authentication(@PathParam("version") String version, @Context ServletContext context){
        Map<String, String> result = new HashMap<>();

        result.put("devices", context.getContextPath() + "/rest/" + version + "/devices");
        result.put("x-auth-method", SUPPORTED_AUTHENTICATION);

        return result;
    }

}
