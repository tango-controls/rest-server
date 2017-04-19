package org.tango.web.server.filters;

import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tango.client.ez.proxy.NoSuchCommandException;
import org.tango.client.ez.proxy.TangoProxyException;
import org.tango.rest.entities.Failures;
import org.tango.web.server.AccessControl;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

/**
 * @author Ingvord
 * @since 01.07.14
 */
@Provider
@PreMatching
public class AccessControlFilter implements ContainerRequestFilter {
    private static final Logger LOG = LoggerFactory.getLogger(AccessControlFilter.class);

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        HttpServletRequest httpServletRequest = ResteasyProviderFactory.getContextData(HttpServletRequest.class);
        String user = httpServletRequest.getRemoteUser();
        if (user == null) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity("Anonymous access is restricted. Provide username and password.").build());
            return;
        }

        ServletContext servletContext = ResteasyProviderFactory.getContextData(ServletContext.class);

        AccessControl accessControl = (AccessControl) servletContext.getAttribute(AccessControl.TANGO_ACCESS);
        if(accessControl == null) return;//TODO configure via interface 'use AccessControl'
        try {
            UriInfo uriInfo = requestContext.getUriInfo();

            MultivaluedMap<String, String> pathParams = uriInfo.getPathParameters();
            String domain = pathParams.getFirst("domain");
            String family = pathParams.getFirst("family");
            String member = pathParams.getFirst("member");

            String device = domain + "/" + family + "/" + member;
            //workaround jsonp limitation
            String method;
            method = httpServletRequest.getParameter("_method");
            if(method == null) method = requestContext.getMethod();
            switch (method) {
                case "GET":
                    if (!accessControl.checkUserCanRead(user, httpServletRequest.getRemoteAddr(), device)){
                        String msg = String.format("User %s does not have read access to %s", user, device);
                        requestContext.abortWith(Response.status(Response.Status.METHOD_NOT_ALLOWED).entity(msg).build());
                        LOG.info(msg);
                    }

                    break;
                case "PUT":
                case "POST":
                case "DELETE":
                    if (!accessControl.checkUserCanWrite(user, httpServletRequest.getRemoteAddr(), device)){
                        String msg = String.format("User %s does not have write access to %s", user, device);
                        requestContext.abortWith(Response.status(Response.Status.METHOD_NOT_ALLOWED).entity(msg).build());
                        LOG.info(msg);
                    }
                    break;
                default:
                    requestContext.abortWith(Response.status(Response.Status.METHOD_NOT_ALLOWED).build());
                    LOG.info("Method is not allowed: " + method);
            }

        } catch (NoSuchCommandException e) {
            requestContext.abortWith(Response.status(Response.Status.BAD_REQUEST).entity(Failures.createInstance(e)).build());
        } catch (TangoProxyException e) {
            requestContext.abortWith(Response.serverError().entity(Failures.createInstance(e)).build());
        }
    }
}
