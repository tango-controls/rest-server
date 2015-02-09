package org.tango.web.server.filters;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 09.02.2015
 */
public class JsonpAuthenticationFilter implements Filter {
    //TODO customizable default user
    public static final String DEFAULT_USER = "*";

    public void destroy() {
    }

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws ServletException, IOException {
        JsonpRequestWrapper requestWrapper = new JsonpRequestWrapper((HttpServletRequest)req);
        chain.doFilter(requestWrapper, resp);
    }

    public void init(FilterConfig config) throws ServletException {
        //TODO read default user
    }

    private static class JsonpRequestWrapper extends HttpServletRequestWrapper {
        /**
         * Constructs a request object wrapping the given request.
         *
         * @param request
         * @throws IllegalArgumentException if the request is null
         */
        public JsonpRequestWrapper(HttpServletRequest request) {
            super(request);
        }

        @Override
        public String getRemoteUser() {
            return DEFAULT_USER;
        }
    }

}
