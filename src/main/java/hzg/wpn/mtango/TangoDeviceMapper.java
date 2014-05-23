package hzg.wpn.mtango;

import com.google.code.simplelrucache.ConcurrentLruCache;
import hzg.wpn.tango.client.proxy.TangoProxies;
import hzg.wpn.tango.client.proxy.TangoProxy;
import hzg.wpn.tango.client.proxy.TangoProxyException;

import javax.servlet.*;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

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
    private static final long TTL = TimeUnit.MILLISECONDS.convert(30, TimeUnit.MINUTES);

    private final TangoProxyPool proxyPool = new TangoProxyPool();

    private DatabaseDs db;

    public void destroy() {
    }

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws ServletException, IOException {
        String devname = req.getParameter(PARAMETER_DEVNAME);
        if(devname == null) throw new ServletException("No remote device was specified.");

        try {
            TangoProxy proxy = proxyPool.getProxy(devname);

            req.setAttribute(ATTR_TANGO_PROXY,proxy);

            chain.doFilter(req, resp);
        } catch (TangoProxyException e) {
            throw new ServletException(e);
        }
    }

    public void init(FilterConfig config) throws ServletException {
        this.db = (DatabaseDs) config.getServletContext().getAttribute(TangoProxyLauncher.TANGO_DB);
    }

    //TODO @ThreadSafe
    public class TangoProxyPool {
        private final ConcurrentLruCache<String,TangoProxy> cache = new ConcurrentLruCache<>(INITIAL_POOL_CAPACITY, TTL);

        public TangoProxy getProxy(String devname) throws TangoProxyException{
            TangoProxy proxy;
            if((proxy = cache.get(devname)) == null){
                String url = db.getDeviceAddress(devname);
                proxy = TangoProxies.newDeviceProxyWrapper(url);
            }
            cache.put(devname,proxy);
            return proxy;
        }
    }
}
