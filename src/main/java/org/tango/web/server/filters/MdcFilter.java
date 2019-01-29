package org.tango.web.server.filters;

import org.slf4j.MDC;
import org.tango.TangoRestServer;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.ext.Provider;

/**
 * Sets context map for MDC
 *
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 1/29/19
 */
@Provider
@PreMatching
public class MdcFilter implements ContainerRequestFilter {
    private final TangoRestServer server;

    public MdcFilter(TangoRestServer server) {
        this.server = server;
    }


    @Override
    public void filter(ContainerRequestContext containerRequestContext) {
        MDC.setContextMap(server.getMdcContextMap());
    }
}
