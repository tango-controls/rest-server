package hzg.wpn.mtango;

import hzg.wpn.tango.client.proxy.TangoProxies;
import hzg.wpn.tango.client.proxy.TangoProxy;
import hzg.wpn.tango.client.proxy.TangoProxyException;

import javax.annotation.concurrent.ThreadSafe;
import javax.servlet.*;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.*;

/**
 * This class looks up for the TangoProxy instance for the device specified in devname parameter of the request.
 * If there is no such instance it creates a new one.
 *
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 23.05.14
 */
public class TangoDeviceMapper implements Filter {

    public static final String PARAMETER_DEVNAME = "devname";
    public static final int INITIAL_POOL_CAPACITY = 100;
    public static final String ATTR_TANGO_PROXY = "tango.proxy";
    public static final long DELAY = 30L;


    private final TangoProxyPool proxyPool = new TangoProxyPool();

    private DatabaseDs db;

    public void destroy() {
    }

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws ServletException, IOException {
        String devname = req.getParameter(PARAMETER_DEVNAME);
        if (devname == null) throw new ServletException("No remote device was specified.");

        try {
            TangoProxy proxy = proxyPool.getProxy(devname);

            req.setAttribute(ATTR_TANGO_PROXY, proxy);

            chain.doFilter(req, resp);
        } catch (TangoProxyException e) {
            throw new ServletException(e);
        }
    }

    public void init(FilterConfig config) throws ServletException {
        this.db = (DatabaseDs) config.getServletContext().getAttribute(TangoProxyLauncher.TANGO_DB);
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
                        if ((current - entry.getValue().longValue()) > 10000) { //10s
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
         * @return a TangoProxy
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
}
