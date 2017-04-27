package org.tango.web.server.filters;

import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tango.client.ez.proxy.NoSuchCommandException;
import org.tango.client.ez.proxy.TangoProxyException;
import org.tango.web.server.AccessControl;
import org.tango.web.server.exception.mapper.GeneralExceptionMapper;
import org.tango.web.server.exception.mapper.NoSuchCommand;

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
    private final Logger logger = LoggerFactory.getLogger(AccessControlFilter.class);

    private final AccessControl accessControl;

    public AccessControlFilter(AccessControl accessControl) {
        this.accessControl = accessControl;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        HttpServletRequest httpServletRequest = ResteasyProviderFactory.getContextData(HttpServletRequest.class);
        String user = httpServletRequest.getRemoteUser();
        if (user == null) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity("Anonymous access is restricted. Provide username and password.").build());
            return;
        }

        try {
            UriInfo uriInfo = requestContext.getUriInfo();

            MultivaluedMap<String, String> pathParams = uriInfo.getPathParameters();
            String domain = pathParams.getFirst("domain");
            String family = pathParams.getFirst("family");
            String member = pathParams.getFirst("member");

            String device = domain + "/" + family + "/" + member;
            String method = requestContext.getMethod();
            switch (method) {
                case "GET":
                    if (!accessControl.checkUserCanRead(user, httpServletRequest.getRemoteAddr(), device)){
                        String msg = String.format("User %s does not have read access to %s", user, device);
                        requestContext.abortWith(Response.status(Response.Status.METHOD_NOT_ALLOWED).entity(msg).build());
                        logger.debug(msg);
                    }

                    break;
                case "PUT":
                case "POST":
                case "DELETE":
                    if (!accessControl.checkUserCanWrite(user, httpServletRequest.getRemoteAddr(), device)){
                        String msg = String.format("User %s does not have write access to %s", user, device);
                        requestContext.abortWith(Response.status(Response.Status.METHOD_NOT_ALLOWED).entity(msg).build());
                        logger.debug(msg);
                    }
                    break;
                default:
                    requestContext.abortWith(Response.status(Response.Status.METHOD_NOT_ALLOWED).build());
                    logger.debug("Method is not allowed: " + method);
            }

        } catch (NoSuchCommandException e) {
            assert false;
            requestContext.abortWith(new NoSuchCommand().toResponse(e));
        } catch (TangoProxyException e) {
            requestContext.abortWith(new GeneralExceptionMapper().toResponse(e));
        }
    }
}
