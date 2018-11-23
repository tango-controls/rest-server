package org.tango.rest;

import com.google.common.collect.Maps;
import org.tango.rest.rc4.Rc4ApiImpl;
import org.tango.rest.rc5.Rc5ApiImpl;

import javax.annotation.Resource;
import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 27.11.2015
 */
@Path("/rest")
@Produces("application/json")
public class EntryPoint {
    private final Map<String, Class<?>> supportedVersions;
    @Context
    private UriInfo uriInfo;

    {
        Map<String, Class<?>> supportedVersions = Maps.newHashMap();

        supportedVersions.put("rc4", Rc4ApiImpl.class);
        supportedVersions.put("rc5", Rc5ApiImpl.class);

        this.supportedVersions = supportedVersions;
    }

    @GET
    public Map<String,String> versions(@Context ServletContext context){
        Map<String,String> result = new HashMap<>();

        for(String version : supportedVersions.keySet()){
            result.put(version, uriInfo.getAbsolutePath() + "/" + version);
        }

        return result;
    }

    @Path("/{version}")
    public Object getVersion(@Context ResourceContext rc, @PathParam("version") String version){
        Class<?> versionEngine = supportedVersions.get(version);
        if(versionEngine == null) throw new NotFoundException("this implementation does not support version " + version);
        return rc.getResource(versionEngine);
    }
}
