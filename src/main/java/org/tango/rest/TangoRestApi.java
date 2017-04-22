package org.tango.rest;

import org.jboss.resteasy.plugins.cache.server.ServerCacheFeature;
import org.jboss.resteasy.plugins.interceptors.CorsFilter;
import org.tango.web.server.TangoContext;
import org.tango.web.server.cache.SimpleBinaryCache;
import org.tango.web.server.providers.AttributeValueCacheProvider;
import org.tango.web.server.providers.StaticValueCacheProvider;

import javax.servlet.ServletContext;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import java.util.HashSet;
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
        Set<Class<?>> classes = new HashSet<>();

        classes.add(EntryPoint.class);

        return classes;
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> singletons = new HashSet<>();

        // = = = CORS = = =
        CorsFilter cors = new CorsFilter();
        cors.getAllowedOrigins().add("*");
        cors.setAllowCredentials(true);
        cors.setAllowedMethods("GET,POST,PUT,DELETE,HEAD");
        cors.setCorsMaxAge(1209600);
        cors.setAllowedHeaders("Origin,Accept,X-Requested-With,Content-Type,Access-Control-Request-Method,Access-Control-Request-Headers,Authorization,Accept-Encoding,Accept-Language,Access-Control-Request-Method,Cache-Control,Connection,Host,Referer,User-Agent");
        singletons.add(cors);

        // = = = Cache = = =
        //TODO inject
        TangoContext tangoContext = new TangoContext();
        SimpleBinaryCache cache = new SimpleBinaryCache(1000);
        tangoContext.isCacheEnabled = true;
        tangoContext.serverSideCacheExpirationDelay = 30000L;
//        SimpleCacheFilter cacheFilter = new SimpleCacheFilter(tangoContext, cache);//TODO inject TangoContext
//        singletons.add(cacheFilter);
//        ResponseCacheWriterInterceptor responseCacheWriterInterceptor = new ResponseCacheWriterInterceptor(cache);
//        singletons.add(responseCacheWriterInterceptor);


        singletons.add(new ServerCacheFeature(cache));
        singletons.add(new AttributeValueCacheProvider());
        singletons.add(new StaticValueCacheProvider());

        return singletons;
    }
}
