package org.tango.web.server.filters;

import com.google.common.base.Joiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tango.client.ez.proxy.TangoProxyException;
import org.tango.web.server.AccessControl;
import org.tango.web.server.DatabaseDs;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author Ingvord
 * @since 01.07.14
 */
public class AuthenticationFilter implements Filter {
    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationFilter.class);

    public void destroy() {
        LOG.info("AuthenticationFilter is destroyed.");
    }

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws ServletException, IOException {
        String user = ((HttpServletRequest) req).getRemoteUser();
        if (user == null) throw new NullPointerException("user can not be null"); //TODO client friendly response
        AccessControl accessControl = (AccessControl) req.getServletContext().getAttribute(AccessControl.TANGO_ACCESS);
        try {
            String requestURI = ((HttpServletRequest) req).getRequestURI();
            String device = getDevice(requestURI);
            switch (((HttpServletRequest) req).getMethod()) {
                case "GET":
                    if (accessControl.checkUserCanRead(user, req.getRemoteAddr(), device))
                        chain.doFilter(req, resp);
                    else
                        throw new IllegalAccessError(String.format("User %s does not have access to %s", user, device));//TODO send client friendly response
                case "PUT":
                    if (accessControl.checkUserCanWrite(user, req.getRemoteAddr(), device))
                        chain.doFilter(req, resp);
                    else
                        throw new IllegalAccessError(String.format("User %s does not have access to %s", user, device));//TODO send client friendly response
                default:
                    throw new IllegalAccessError("Method is not allowed: " + ((HttpServletRequest) req).getMethod());
            }

        } catch (TangoProxyException e) {
            throw new ServletException(e);
        }
    }

    private String getDevice(String uri) {
        String[] parts = uri.split("/");
        List<String> partsList = Arrays.asList(parts);
        if (partsList.contains("devices")) return DatabaseDs.DEFAULT_ID;
        int marker = partsList.indexOf("device");
        return Joiner.on('/').join(Arrays.copyOfRange(parts, marker + 1, marker + 4));
    }

    public void init(FilterConfig config) throws ServletException {
        if (config.getServletContext().getAttribute(AccessControl.TANGO_ACCESS) == null)
            throw new ServletException("TangoAccessControl is null");
    }

}
