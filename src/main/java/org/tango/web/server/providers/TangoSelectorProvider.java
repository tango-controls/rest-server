package org.tango.web.server.providers;

import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.tango.rest.rc4.entities.Failures;
import org.tango.web.server.TangoProxyPool;
import org.tango.web.server.binding.RequiresTangoSelector;
import org.tango.web.server.util.TangoSelector;
import org.tango.web.server.util.Wildcard;
import org.tango.web.server.util.WildcardExtractor;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.List;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11/16/18
 */
@Provider
@RequiresTangoSelector
public class TangoSelectorProvider implements ContainerRequestFilter {
    public static final String WILDCARD = "wildcard";
    private TangoProxyPool proxyPool;

    public TangoSelectorProvider(TangoProxyPool proxyPool) {
        this.proxyPool = proxyPool;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        UriInfo uriInfo = requestContext.getUriInfo();

        List<String> queryWildcards = uriInfo.getQueryParameters().get(WILDCARD);

        List<Wildcard> wildcards = new WildcardExtractor().extractWildcards(queryWildcards);

        if(wildcards.isEmpty()) {
            requestContext.abortWith(
                    Response.status(Response.Status.BAD_REQUEST).entity(Failures.createInstance("wildcard parameter is missing!")).build());
            return;
        }

        ResteasyProviderFactory.pushContext(TangoSelector.class, new TangoSelector(wildcards, proxyPool));

    }


}
