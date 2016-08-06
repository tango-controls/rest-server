package org.tango.rest;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 27.11.2015
 */
@Path("/") //relative to rest
//TODO
//@ApplicationPath("rest")
@Produces("application/json")
public class EntryPoint /* extends Application*/ {
    @Context
    private UriInfo uriInfo;

    private final List<String> supportedVersions = new ArrayList<>(3);
    {
        supportedVersions.add("rc3");
        supportedVersions.add("rc2");
        supportedVersions.add("mtango");
    }

    @GET
    public Map<String,String> versions(@Context ServletContext context){
        Map<String,String> result = new HashMap<>();

        for(String version : supportedVersions){
            result.put(version, uriInfo.getAbsolutePath() + version);
        }

        return result;
    }
}
