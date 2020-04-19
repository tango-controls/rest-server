/*
 * Copyright 2019 Tango Controls
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tango.rest;

import com.google.common.collect.Maps;
import org.tango.rest.rc4.Rc4ApiImpl;
import org.tango.rest.v10.V10ApiImpl;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Context;
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
        supportedVersions.put("v10", V10ApiImpl.class);
        supportedVersions.put("v11", V10ApiImpl.class);

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
