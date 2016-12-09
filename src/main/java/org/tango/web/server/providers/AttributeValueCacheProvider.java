package org.tango.web.server.providers;

import org.tango.web.server.TangoContext;

import javax.ws.rs.ext.Provider;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 15.12.2015
 */
@Provider
@AttributeValue
public class AttributeValueCacheProvider extends AbstractCacheProvider {
    @Override
    protected long getDelay(TangoContext context) {
        return context.attributeValueExpirationDelay;
    }
}
