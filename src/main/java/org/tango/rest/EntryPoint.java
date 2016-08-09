package org.tango.rest;

import org.tango.rest.rc3.Rc3ApiImpl;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.HashMap;
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

    private final Map<String, Object> supportedVersions = new HashMap<>(3);
    {
        supportedVersions.put("rc3", new Rc3ApiImpl());
//        supportedVersions.put("rc2", new Rc2ApiImpl());
//        supportedVersions.put("mtango", new MtangoImpl());
    }

    @GET
    public Map<String,String> versions(@Context ServletContext context){
        Map<String,String> result = new HashMap<>();

        for(String version : supportedVersions.keySet()){
            result.put(version, uriInfo.getAbsolutePath() + version);
        }

        return result;
    }

    @Path("/{version}")
    public Object getVersion(@PathParam("version") String version){
        Object versionEngine = supportedVersions.get(version);
        if(versionEngine == null) return Response.status(Response.Status.NOT_FOUND);
        return versionEngine;
    }
}
