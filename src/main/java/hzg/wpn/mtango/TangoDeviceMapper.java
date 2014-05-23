package hzg.wpn.mtango;

import hzg.wpn.tango.client.proxy.TangoProxies;
import hzg.wpn.tango.client.proxy.TangoProxy;
import hzg.wpn.tango.client.proxy.TangoProxyException;

import javax.servlet.*;
import java.io.IOException;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 23.05.14
 */
public class TangoDeviceMapper implements Filter {

    public static final String PARAMETER_DEVNAME = "devname";
    public static final int INITIAL_POOL_CAPACITY = 100;
    public static final String ATTR_TANGO_PROXY = "tango.proxy";

    private final TangoProxyPool proxyPool = new TangoProxyPool();

    public void destroy() {
    }

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws ServletException, IOException {
        String devname = req.getParameter(PARAMETER_DEVNAME);
        if(devname == null) throw new ServletException("No remote device was specified.");

        DatabaseDs db = (DatabaseDs) req.getServletContext().getAttribute(TangoProxyLauncher.TANGO_DB);

        try {
            String address = db.getDeviceAddress(devname);

            TangoProxy proxy = proxyPool.getProxy(address);

            req.setAttribute(ATTR_TANGO_PROXY,proxy);

            chain.doFilter(req, resp);
        } catch (TangoProxyException e) {
            throw new ServletException(e);
        }
    }

    public void init(FilterConfig config) throws ServletException {

    }

    //TODO @ThreadSafe
    public static class TangoProxyPool {
        //TODO cache

        public TangoProxy getProxy(String url) throws TangoProxyException{
            TangoProxy proxy;
            proxy = TangoProxies.newDeviceProxyWrapper(url);
            return proxy;
        }
    }
}
