package org.tango.web.server.providers;

import com.google.common.base.Objects;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.tango.client.ez.proxy.TangoProxy;
import org.tango.client.ez.proxy.TangoProxyException;
import org.tango.rest.response.Responses;
import org.tango.web.server.TangoContext;

import javax.annotation.concurrent.ThreadSafe;
import javax.servlet.ServletContext;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 04.12.2015
 */
@Provider
public class TangoProxyProvider implements ContainerRequestFilter {
    @Context
    public ServletContext servletContext;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        UriInfo uriInfo = requestContext.getUriInfo();
        MultivaluedMap<String, String> pathParams = uriInfo.getPathParameters();
        String host = pathParams.getFirst("host");
        String port = pathParams.getFirst("port");
        String domain = pathParams.getFirst("domain");
        String family = pathParams.getFirst("family");
        String member = pathParams.getFirst("member");

        TangoProxy result = null;
        try{
            if(host != null && port != null)
                domain = "tango://" + host + ":" + port + "/" + domain;
            if(domain != null && family != null && member != null)
                result = map(domain, family, member);

            ResteasyProviderFactory.pushContext(TangoProxy.class, result);
        } catch (TangoProxyException e){
            requestContext.abortWith(Response.ok(Responses.createFailureResult(e)).build());
        }
    }

    private TangoProxy map(String devname) throws TangoProxyException {
        TangoProxy proxy = ((TangoContext)servletContext.getAttribute(TangoContext.TANGO_CONTEXT)).proxyPool.getProxy(devname);
        return proxy;
    }

    private TangoProxy map(String domain, String family, String member) throws TangoProxyException {
        return map(domain + "/" + family + "/" + member);
    }
}
