package org.tango.web.server.filters;

import fr.esrf.Tango.DevFailed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tango.web.server.Responses;

import javax.servlet.*;
import java.io.IOException;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 04.12.2015
 */
public class FailsafeFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(FailsafeFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            chain.doFilter(request, response);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            Responses.sendFailure(e, response.getWriter());
        }
    }

    @Override
    public void destroy() {

    }
}
