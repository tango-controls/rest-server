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

import org.jboss.resteasy.plugins.cache.server.ServerCacheFeature;
import org.jboss.resteasy.plugins.interceptors.CorsFilter;
import org.tango.TangoRestServer;
import org.tango.web.server.AccessControl;
import org.tango.web.server.cache.SimpleBinaryCache;
import org.tango.web.server.filters.*;
import org.tango.web.server.interceptors.ImageAttributeValueProvider;
import org.tango.web.server.interceptors.JsonpResponseWrapper;
import org.tango.web.server.interceptors.TangoAttributeValueInterceptor;
import org.tango.web.server.providers.*;
import org.tango.web.server.v10.readers.CommandInOutBodyReader;
import org.tango.web.server.writers.PlainTextWriter;

import javax.servlet.ServletContext;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 4/17/17
 */
@ApplicationPath("/rest")
public class TangoRestApi extends Application {
    @Context
    private ServletContext servletContext;

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new LinkedHashSet<>();

        classes.add(EntryPoint.class);

        return classes;
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> singletons = new LinkedHashSet<>();

        // = = = Utils = = =
        singletons.add(new MdcFilter(getTangoRestServer()));

        // = = = CORS = = =
        CorsFilter cors = getCorsFilter();
        singletons.add(cors);

        // = = = AccessControl = = =
        AccessControl accessControl = (AccessControl) servletContext.getAttribute(AccessControl.class.getName());
        if (accessControl != null) {
            singletons.add(new AccessControlFilter(accessControl));
        }

        // = = = Providers = = =
        singletons.add(new TangoContextProvider(getTangoRestServer()));
        singletons.add(new TangoDatabaseProvider(getTangoRestServer()));
        singletons.add(new TangoDeviceProxyProvider(getTangoRestServer()));
        singletons.add(new TangoAttributeProxyProvider(getTangoRestServer()));
        singletons.add(new TangoCommandProxyProvider(getTangoRestServer()));
        singletons.add(new TangoPipeProxyProvider());
        singletons.add(new DevicesTreeContextProvider());
        singletons.add(new TangoSelectorProvider(getTangoRestServer()));
        singletons.add(new PartitionProvider());

        // = = = Interceptors = = =
        singletons.add(new ImageAttributeValueProvider());
        singletons.add(new TangoAttributeValueInterceptor());

        // = = = Cache = = =
        SimpleBinaryCache cache = new SimpleBinaryCache(getTangoRestServer().getTomcatCacheSize());

        singletons.add(new ServerCacheFeature(cache));
        singletons.add(new DynamicValueCacheControlProvider(getTangoRestServer()));
        singletons.add(new StaticValueCacheControlProvider(getTangoRestServer()));

        // = = = JsonP  = = =
        singletons.add(new JsonpMethodFilter());
        singletons.add(new JsonpResponseWrapper());

        singletons.add(new CommandInOutBodyReader());
        singletons.add(new PlainTextWriter());

        return singletons;
    }

    public static CorsFilter getCorsFilter() {
        CorsFilter cors = new CorsFilter();
        cors.getAllowedOrigins().add("*");
        cors.setAllowCredentials(true);
        cors.setAllowedMethods("GET,POST,PUT,DELETE,HEAD");
        cors.setCorsMaxAge(1209600);
        cors.setAllowedHeaders("Origin,Accept,X-Requested-With,Content-Type,Access-Control-Request-Method,Access-Control-Request-Headers,Authorization,Accept-Encoding,Accept-Language,Access-Control-Request-Method,Cache-Control,Connection,Host,Referer,User-Agent");
        return cors;
    }

    private TangoRestServer getTangoRestServer(){
        return (TangoRestServer) servletContext.getAttribute(TangoRestServer.class.getName());
    }
}
