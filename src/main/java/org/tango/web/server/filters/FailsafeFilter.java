package org.tango.web.server.filters;

import fr.esrf.Tango.DevFailed;
import org.tango.web.server.Responses;

import javax.servlet.*;
import java.io.IOException;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 04.12.2015
 */
public class FailsafeFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            chain.doFilter(request, response);
        } catch (Exception e) {
            Responses.sendFailure(e, response.getWriter());
        }
    }

    @Override
    public void destroy() {

    }
}
