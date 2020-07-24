package org.tango.web.server.providers;

import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.tango.web.server.TangoProxiesCache;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 24.07.2020
 */
@Provider
public class TangoProxiesCacheProvider implements ContainerRequestFilter {
    private final ThreadLocal<TangoProxiesCache> proxies;

    public TangoProxiesCacheProvider(ThreadLocal<TangoProxiesCache> proxies) {
        this.proxies = proxies;
    }


    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {
        ResteasyProviderFactory.pushContext(TangoProxiesCache.class, proxies.get());
    }
}
