package org.tango.web.server.filters;

import org.tango.TangoRestServer;
import org.tango.web.server.binding.StaticValue;

import javax.ws.rs.ext.Provider;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 15.12.2015
 */
@Provider
@StaticValue
public class StaticValueCacheControlProvider extends AbstractCacheControlProvider {
    public StaticValueCacheControlProvider(TangoRestServer tangoRestServer) {
        super(tangoRestServer);
    }

    @Override
    protected long getDelay() {
        return tangoRestServer.getStaticValueExpirationDelay();
    }
}
