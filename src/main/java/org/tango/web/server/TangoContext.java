package org.tango.web.server;

import com.google.common.base.Objects;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tango.client.ez.proxy.TangoProxies;
import org.tango.client.ez.proxy.TangoProxy;
import org.tango.client.ez.proxy.TangoProxyException;

import javax.annotation.concurrent.ThreadSafe;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 14.12.2015
 */
public class TangoContext {
    public static final String TANGO_CONTEXT = "org.tango.rest.server.context";
    public static final int INITIAL_POOL_CAPACITY = 100;
    public static final long DELAY = 30L;
    public static final String SYS_DATABASE_2 = "sys/database/2";
    public static final String TANGO_LOCALHOST = "localhost:10000";
    public final TangoProxyPool hostsPool = new TangoProxyPool();
    public final TangoProxyPool proxyPool = new TangoProxyPool();
    public final ConcurrentMap<String, TangoProxyCreationPolicy> tangoProxyCreationPolicies = new ConcurrentHashMap<>();
    private final Logger logger = LoggerFactory.getLogger(TangoContext.class);
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(
            new ThreadFactoryBuilder()
                    .setNameFormat("tango-proxies-pool-manager for TangoContext@" + this.hashCode())
                    .setDaemon(true)
                    .build());
    public volatile long tangoProxyKeepAliveDelay = DELAY;
    public volatile TimeUnit tangoProxyKeepAliveDelayTimeUnit = TimeUnit.MINUTES;
    public volatile long attributeValueExpirationDelay = 200L;
    public volatile long staticDataExpirationDelay = 30000L;
    public volatile int cacheCapacity;
    public volatile boolean isCacheEnabled = false;
    //defines tangoHost specified at start via tango rest server device properties or environmental variable
    public volatile String tangoHost = TANGO_LOCALHOST;
    //defines tango db device name is used for db proxy lookup
    public volatile String tangoDbName = SYS_DATABASE_2;
    //defines full path to tango db to which application connects at start
    public volatile String tangoDb = "tango://" + TANGO_LOCALHOST + "/" + SYS_DATABASE_2;

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("tangoHost", tangoHost)
                .add("tangoDb", tangoDb)
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

    /**
     *
     * @return tango db proxy created at application start
     * @throws TangoProxyException
     */
    public TangoProxy getHostProxy() throws TangoProxyException {
        return hostsPool.getProxy("tango://" + tangoHost + "/" + tangoDbName);
    }

    /**
     * @author ingvord
     * @since 8/6/16
     */
    @ThreadSafe
    public class TangoProxyPool {
        private final ConcurrentMap<String, FutureTask<TangoProxy>> cache = new ConcurrentHashMap<>(INITIAL_POOL_CAPACITY);
        private final ConcurrentMap<String, Long> timestamps = new ConcurrentHashMap<>(INITIAL_POOL_CAPACITY);


        {
                    TangoContext.this.scheduler.schedule(new Runnable() {
                        @Override
                        public void run() {
                            long current = System.currentTimeMillis();
                            for (Map.Entry<String, Long> entry : timestamps.entrySet()) {
                                if ((current - entry.getValue().longValue()) > TimeUnit.MILLISECONDS.convert(TangoContext.this.tangoProxyKeepAliveDelay, TangoContext.this.tangoProxyKeepAliveDelayTimeUnit)) {
                                    cache.remove(entry.getKey());
                                }
                            }
                        }
                    }, DELAY, TimeUnit.SECONDS);
        }

        /**
         * Implementation guarantees that only one proxy instance for each remote Tango device will be created.
         *
         * @param devname
         * @return a Launcher
         * @throws TangoProxyException
         */
        public TangoProxy getProxy(final String devname) throws TangoProxyException {
            FutureTask<TangoProxy> ft = cache.get(devname);
            if (ft == null) {
                Callable<TangoProxy> callable = new Callable<TangoProxy>() {
                    public TangoProxy call() throws Exception {
    //                            String url = devname.startsWith("tango://") ? devname : DeviceMapper.this.ctx.databaseDs.getDeviceAddress(devname);//TODO db NPE?
                        TangoProxy proxy = TangoProxies.newDeviceProxyWrapper(devname);
                                TangoProxyCreationPolicy tangoProxyCreationPolicy = TangoContext.this.tangoProxyCreationPolicies.get(proxy.getName());
                                if(tangoProxyCreationPolicy != null) tangoProxyCreationPolicy.apply(proxy);
                        return proxy;
                    }
                };

                FutureTask<TangoProxy> f = new FutureTask<>(callable);
                ft = cache.putIfAbsent(devname, f);
                if (ft == null) {
                    ft = f;
                    ft.run();
                }
            }
            try {
                timestamps.put(devname, System.currentTimeMillis());
                return ft.get();
            } catch (InterruptedException | ExecutionException e) {
                cache.remove(devname);
                logger.error("Failed to get proxy for " + devname, e);
                throw new TangoProxyException("Failed to get proxy for " + devname, e.getCause());
            }
        }

        public List<TangoProxy> proxies() throws TangoProxyException {
            List<TangoProxy> result = new ArrayList<>();

            for (String name : cache.keySet()) {
                result.add(getProxy(name));
            }

            return result;
        }
    }
}
