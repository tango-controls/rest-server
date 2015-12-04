package org.tango.web.server.providers;

import org.jboss.resteasy.core.interception.PostMatchContainerRequestContext;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.jboss.resteasy.spi.ResteasyUriInfo;
import org.tango.client.ez.proxy.TangoProxy;
import org.tango.client.ez.proxy.TangoProxyException;
import org.tango.web.server.DatabaseDs;
import org.tango.web.server.DeviceMapper;
import org.tango.web.server.Responses;

import javax.servlet.ServletContext;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 04.12.2015
 */
@Provider
@TangoProxyBackend
public class TangoProxyProvider implements ContainerRequestFilter {
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        HttpRequest httpRequest = ((PostMatchContainerRequestContext) requestContext).getHttpRequest();
        ResteasyUriInfo uriInfo = (ResteasyUriInfo) httpRequest.getUri();
        MultivaluedMap<String, String> pathParams = uriInfo.getPathParameters();
        String domain = pathParams.getFirst("domain");
        String family = pathParams.getFirst("family");
        String member = pathParams.getFirst("member");

        TangoProxy result = null;
        try{
            if(domain != null && family != null && member != null)
                result = ((DeviceMapper) ResteasyProviderFactory.getContextData(ServletContext.class).getAttribute(DeviceMapper.TANGO_MAPPER)).map(domain, family, member);

            ResteasyProviderFactory.pushContext(TangoProxy.class, result);
        } catch (TangoProxyException e){
            requestContext.abortWith(Response.ok(Responses.createFailureResult(e)).build());
        }
    }
}
