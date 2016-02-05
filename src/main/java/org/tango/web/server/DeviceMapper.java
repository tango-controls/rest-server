package org.tango.web.server;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.tango.client.ez.proxy.TangoProxies;
import org.tango.client.ez.proxy.TangoProxy;
import org.tango.client.ez.proxy.TangoProxyException;

import javax.annotation.concurrent.ThreadSafe;
import javax.servlet.ServletContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * This class looks up for the Launcher instance for the device specified in devname parameter of the request.
 * If there is no such instance it creates a new one.
 * <p/>
 * Only on instance of thos class should is allowed. Servlets must acquire it from ServletContext
 *
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 23.05.14
 */
@ThreadSafe
public class DeviceMapper {
    public static final int INITIAL_POOL_CAPACITY = 100;
    public static final long DELAY = 30L;
    public static final String TANGO_MAPPER = "tango.mapper";

    private final TangoProxyPool proxyPool = new TangoProxyPool();

    private final TangoContext ctx;

    public DeviceMapper(TangoContext ctx) {
        Preconditions.checkNotNull(ctx.databaseDs);
        this.ctx = ctx;
    }

    public TangoProxy map(String devname) throws TangoProxyException {
        TangoProxy proxy = proxyPool.getProxy(devname);
        return proxy;
    }

    public TangoProxy map(String domain, String family, String member) throws TangoProxyException {
        return map(domain + "/" + family + "/" + member);
    }

    public List<TangoProxy> proxies() throws TangoProxyException {
        return proxyPool.proxies();
    }


    static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(
            new ThreadFactoryBuilder()
                    .setNameFormat("tango-proxies-pool-manager")
                    .build());

    @ThreadSafe
    public class TangoProxyPool {
        private final ConcurrentMap<String, FutureTask<TangoProxy>> cache = new ConcurrentHashMap<>(INITIAL_POOL_CAPACITY);
        private final ConcurrentMap<String, Long> timestamps = new ConcurrentHashMap<>(INITIAL_POOL_CAPACITY);


        {
            scheduler.schedule(new Runnable() {
                @Override
                public void run() {
                    long current = System.currentTimeMillis();
                    for (Map.Entry<String, Long> entry : timestamps.entrySet()) {
                        if ((current - entry.getValue().longValue()) > TimeUnit.MILLISECONDS.convert(DeviceMapper.this.ctx.tangoProxyKeepAliveDelay, DeviceMapper.this.ctx.tangoProxyKeepAliveDelayTimeUnit)) {
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
                        String url = DeviceMapper.this.ctx.databaseDs.getDeviceAddress(devname);//TODO db NPE?
                        TangoProxy proxy = TangoProxies.newDeviceProxyWrapper(url);
                        TangoProxyCreationPolicy tangoProxyCreationPolicy = ctx.tangoProxyCreationPolicies.get(proxy.getName());
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
                throw new TangoProxyException("Can not get proxy for " + devname, e);
            }
        }

        public List<TangoProxy> proxies() throws TangoProxyException {
            List<TangoProxy> result = new ArrayList<>();

            for(String name : cache.keySet()){
                result.add(getProxy(name));
            }

            return result;
        }
    }

    public static TangoProxy lookup(String domain, String family, String member, ServletContext context) throws TangoProxyException {
        DeviceMapper mapper = (DeviceMapper) context.getAttribute(DeviceMapper.TANGO_MAPPER);
        return mapper.map(domain + "/" + family + "/" + member);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("proxyPool", proxyPool)
                .toString();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        scheduler.shutdownNow();
    }
}
