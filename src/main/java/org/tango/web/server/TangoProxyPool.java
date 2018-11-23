package org.tango.web.server;

import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevSource;
import fr.esrf.TangoApi.AttributeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tango.client.ez.proxy.TangoProxies;
import org.tango.client.ez.proxy.TangoProxy;
import org.tango.client.ez.proxy.TangoProxyException;

import javax.annotation.concurrent.ThreadSafe;
import java.util.Collection;
import java.util.concurrent.*;

/**
 * @author ingvord
 * @since 8/6/16
 */
@ThreadSafe
public class TangoProxyPool {
    //TODO export as Tango attributes
    public static final int POOL_CAPACITY = 1000;
    public static final long TANGO_PROXY_REMOVAL_DELAY = 7L;
    private static final ScheduledExecutorService MAINTENANCE = Executors.newScheduledThreadPool(1,
            new ThreadFactoryBuilder().setNameFormat("tango-proxy-pool-maintenance-thread").setDaemon(true).build()
    );
    private static final TangoProxyCreationPolicy DEFAULT_CREATION_POLICY = new TangoProxyCreationPolicy(DevSource.CACHE_DEV);
    public final ConcurrentMap<String, TangoProxyPool.TangoProxyCreationPolicy> tangoProxyCreationPolicies = new ConcurrentLinkedHashMap.Builder<String, TangoProxyPool.TangoProxyCreationPolicy>()
            .maximumWeightedCapacity(POOL_CAPACITY)
            .build();
    private final Logger logger = LoggerFactory.getLogger(TangoProxyPool.class);
    private final ConcurrentMap<String, FutureTask<TangoProxy>> cache = new ConcurrentLinkedHashMap.Builder<String, FutureTask<TangoProxy>>()
            .maximumWeightedCapacity(POOL_CAPACITY)
            .build();

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
            Callable<TangoProxy> callable = () -> {
//                            String url = devname.startsWith("tango://") ? devname : DeviceMapper.this.ctx.databaseDs.getDeviceAddress(devname);//TODO db NPE?
                TangoProxy proxy = TangoProxies.newDeviceProxyWrapper(devname);
                TangoProxyCreationPolicy tangoProxyCreationPolicy = tangoProxyCreationPolicies.getOrDefault(proxy.getName(), DEFAULT_CREATION_POLICY);
                tangoProxyCreationPolicy.apply(proxy);
                MAINTENANCE.schedule(() -> {
                    logger.debug("Removing tango proxy for {}", devname);
                    cache.remove(devname);
                }, TANGO_PROXY_REMOVAL_DELAY, TimeUnit.DAYS);
                return proxy;
            };

            FutureTask<TangoProxy> f = new FutureTask<>(callable);
            ft = cache.putIfAbsent(devname, f);
            if (ft == null) {
                ft = f;
                ft.run();
            }
        }
        try {
            return ft.get();
        } catch (ExecutionException e) {
            cache.remove(devname);
            throw new TangoProxyException("Failed to get proxy for " + devname, e.getCause()); //TODO re-throw ExecutionException
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TangoProxyException("Failed to get proxy for " + devname, e.getCause()); //TODO re-throw Exception
        }
    }

    public Collection<String> proxies() throws TangoProxyException {
        return cache.keySet();
    }

    /**
     * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
     * @since 15.12.2015
     */
    public static class TangoProxyCreationPolicy {
        public final ConcurrentMap<String, AttributeInfo> attributesPolicy = new ConcurrentHashMap<>();
        public volatile DevSource source;

        public TangoProxyCreationPolicy(DevSource source) {
            this.source = source;
        }

        public void apply(TangoProxy proxy) throws TangoProxyException {
            try {
                proxy.toDeviceProxy().set_source(source);

                proxy.toDeviceProxy().set_attribute_info(Iterables.toArray(attributesPolicy.values(), AttributeInfo.class));
            } catch (DevFailed devFailed) {
                throw new TangoProxyException("Failed to apply creation policy for proxy " + proxy.getName(), devFailed);
            }
        }
    }
}
