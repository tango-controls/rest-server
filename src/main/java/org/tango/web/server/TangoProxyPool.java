package org.tango.web.server;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
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
    public static final int POOL_CAPACITY = 1000;

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
            return ft.get();
        } catch (InterruptedException | ExecutionException e) {
            cache.remove(devname);
            logger.error("Failed to get proxy for " + devname, e);
            throw new TangoProxyException("Failed to get proxy for " + devname, e.getCause());
        }
    }

    public Collection<String> proxies() throws TangoProxyException {
        return cache.keySet();
    }
}
