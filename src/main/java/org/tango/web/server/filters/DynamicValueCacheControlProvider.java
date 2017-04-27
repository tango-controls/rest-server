package org.tango.web.server.filters;

import org.tango.TangoRestServer;
import org.tango.web.server.binding.DynamicValue;

import javax.ws.rs.ext.Provider;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 15.12.2015
 */
@Provider
@DynamicValue
public class DynamicValueCacheControlProvider extends AbstractCacheControlProvider {
    public DynamicValueCacheControlProvider(TangoRestServer tangoRestServer) {
        super(tangoRestServer);
    }

    @Override
    protected long getDelay() {
        return tangoRestServer.getDynamicValueExpirationDelay();
    }
}
