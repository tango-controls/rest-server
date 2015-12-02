package org.tango.web.server;

import com.google.common.base.Preconditions;
import org.tango.client.ez.proxy.TangoProxies;
import org.tango.client.ez.proxy.TangoProxy;
import org.tango.client.ez.proxy.TangoProxyException;

import javax.annotation.concurrent.ThreadSafe;
import javax.servlet.ServletContext;
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

    private final DatabaseDs db;

    public DeviceMapper(DatabaseDs db) {
        Preconditions.checkNotNull(db);
        this.db = db;
    }

    public TangoProxy map(String devname) throws TangoProxyException {
        TangoProxy proxy = proxyPool.getProxy(devname);
        return proxy;
    }


    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

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
                        if ((current - entry.getValue().longValue()) > TimeUnit.MILLISECONDS.convert(5, TimeUnit.MINUTES)) {
                            cache.remove(entry.getKey());
                        }
                    }
                }
            }, DELAY, TimeUnit.MINUTES);
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
                        String url = db.getDeviceAddress(devname);//TODO db NPE?
                        return TangoProxies.newDeviceProxyWrapper(url);
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
                throw new TangoProxyException(e);
            }
        }
    }

    public static TangoProxy lookup(String domain, String family, String member, ServletContext context) throws TangoProxyException {
        DeviceMapper mapper = (DeviceMapper) context.getAttribute(DeviceMapper.TANGO_MAPPER);
        return mapper.map(domain + "/" + family + "/" + member);
    }
}
