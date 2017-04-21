package org.tango.rest;

import org.jboss.resteasy.plugins.interceptors.CorsFilter;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 4/17/17
 */
@ApplicationPath("/rest")
public class TangoRestApi extends Application {
    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();

        classes.add(EntryPoint.class);

        return classes;
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> result = new HashSet<>();

        CorsFilter cors = new CorsFilter();
        cors.getAllowedOrigins().add("*");
        cors.setAllowCredentials(true);
        cors.setAllowedMethods("GET,POST,PUT,DELETE,HEAD");
        cors.setCorsMaxAge(1209600);
        cors.setAllowedHeaders("Origin,Accept,X-Requested-With,Content-Type,Access-Control-Request-Method,Access-Control-Request-Headers,Authorization,Accept-Encoding,Accept-Language,Access-Control-Request-Method,Cache-Control,Connection,Host,Referer,User-Agent");
        result.add(cors);

        return result;
    }
}
