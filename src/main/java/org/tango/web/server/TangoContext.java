package org.tango.web.server;

import com.google.common.base.Objects;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 14.12.2015
 */
public class TangoContext {
    public static final String TANGO_CONTEXT = "org.tango.rest.server.context";

    public volatile long tangoProxyKeepAliveDelay = 30L;
    public volatile TimeUnit tangoProxyKeepAliveDelayTimeUnit = TimeUnit.MINUTES;

    public volatile long attributeValueExpirationDelay = 200L;
    public volatile long staticDataExpirationDelay = 30000L;

    public volatile DatabaseDs databaseDs;
    public volatile DeviceMapper deviceMapper;
    public volatile AccessControl accessControl;

    public final ConcurrentMap<String, TangoProxyCreationPolicy> tangoProxyCreationPolicies = new ConcurrentHashMap<>();
    public volatile boolean isCacheEnabled;
    public volatile String tangoDbName;
    public volatile String tangoHost;

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("tangoHost", tangoHost)
                .add("accessControl", accessControl)
                .add("tangoProxyKeepAliveDelay", tangoProxyKeepAliveDelay)
                .add("tangoProxyKeepAliveDelayTimeUnit", tangoProxyKeepAliveDelayTimeUnit)
                .add("attributeValueExpirationDelay", attributeValueExpirationDelay)
                .add("staticDataExpirationDelay", staticDataExpirationDelay)
                .add("tangoDbName", tangoDbName)
                .add("databaseDs", databaseDs)
                .add("deviceMapper", deviceMapper)
                .add("tangoProxyCreationPolicies", tangoProxyCreationPolicies)
                .add("isCacheEnabled", isCacheEnabled)
                .toString();
    }
}
