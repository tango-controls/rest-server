package org.tango.web.server.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author ingvord
 * @since 5/25/14@1:54 AM
 */
public class TimeWatcher implements Filter {
    private static final Logger LOG = LoggerFactory.getLogger(TimeWatcher.class);

    public void destroy() {
    }

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws ServletException, IOException {
        //TODO is there any runtime context listeners?
        if ("no".equals(req.getServletContext().getAttribute("mtango.watch.time"))) {
            chain.doFilter(req, resp);
            return;
        }
        LOG.info("Serving request. Stopwatch is active.");
        long start = System.nanoTime();
        chain.doFilter(req, resp);
        long end = System.nanoTime();
        long delta = end - start;
        long delta_ms = TimeUnit.MILLISECONDS.convert(delta, TimeUnit.NANOSECONDS);
        LOG.info("Request processing time (nano):" + delta);
        LOG.info("Request processing time (ms):" + delta_ms);
    }

    public void init(FilterConfig config) throws ServletException {

    }

}
