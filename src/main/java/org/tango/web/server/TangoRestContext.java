package org.tango.web.server;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 23.07.2020
 */
public class TangoRestContext {
    public final AtomicLong dynamicValueExpirationDelay = new AtomicLong(100L);
    public final AtomicLong staticValueExpirationDelay = new AtomicLong(1000L);
    public final AtomicInteger capacity = new AtomicInteger(3000);
    public volatile boolean cacheEnabled = false;

    public boolean isCacheEnabled() {
        return cacheEnabled;
    }
}
