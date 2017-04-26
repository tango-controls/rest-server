package org.tango.web.server;

import com.google.common.base.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tango.client.ez.proxy.TangoProxy;
import org.tango.client.ez.proxy.TangoProxyException;

import java.util.concurrent.*;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 14.12.2015
 */
public class TangoContext {
    public static final String TANGO_CONTEXT = "org.tango.rest.server.context";
    public static final String SYS_DATABASE_2 = "sys/database/2";
    public static final String TANGO_LOCALHOST = "localhost:10000";
    public final TangoProxyPool hostsPool;
    public final TangoProxyPool proxyPool;
    public final ConcurrentMap<String, TangoProxyCreationPolicy> tangoProxyCreationPolicies = new ConcurrentHashMap<>();

    public volatile long tangoProxyKeepAliveDelay = TangoProxyPool.DELAY;
    public volatile TimeUnit tangoProxyKeepAliveDelayTimeUnit = TimeUnit.MINUTES;
    public volatile long attributeValueExpirationDelay = 200L;
    public volatile long staticDataExpirationDelay = 30000L;
    public volatile int cacheCapacity = 100;
    public volatile boolean isCacheEnabled = false;
    //defines tango db device name is used for db proxy lookup
    public volatile String tangoDbName = SYS_DATABASE_2;

    {



        hostsPool = new TangoProxyPool();
        proxyPool = new TangoProxyPool();
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("tangoDbName", tangoDbName)
                .add("tangoProxyKeepAliveDelay", tangoProxyKeepAliveDelay)
                .add("tangoProxyKeepAliveDelayTimeUnit", tangoProxyKeepAliveDelayTimeUnit)
                .add("attributeValueExpirationDelay", attributeValueExpirationDelay)
                .add("staticDataExpirationDelay", staticDataExpirationDelay)
                .add("tangoProxyCreationPolicies", tangoProxyCreationPolicies)
                .add("isCacheEnabled", isCacheEnabled)
                .toString();
    }


    public TangoProxy getHostProxy(String host, String port) throws TangoProxyException {
        return hostsPool.getProxy("tango://" + host + ":" + port + "/" + tangoDbName);
    }

}
