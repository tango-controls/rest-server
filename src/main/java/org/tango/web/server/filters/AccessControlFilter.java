package org.tango.web.server.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tango.client.ez.proxy.TangoProxyException;
import org.tango.web.server.AccessControl;
import org.tango.web.server.util.CommonUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Ingvord
 * @since 01.07.14
 */
public class AccessControlFilter implements Filter {
    private static final Logger LOG = LoggerFactory.getLogger(AccessControlFilter.class);

    public void destroy() {
        LOG.info("AccessControlFilter is destroyed.");
    }

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws ServletException, IOException {
        String user = ((HttpServletRequest) req).getRemoteUser();
        if (user == null) {
            ((HttpServletResponse) resp).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized user!");
            return;
        }

        AccessControl accessControl = (AccessControl) req.getServletContext().getAttribute(AccessControl.TANGO_ACCESS);
        try {
            String requestURI = ((HttpServletRequest) req).getRequestURI();
            String device = CommonUtils.parseDevice(requestURI);
            //workaround jsonp limitation
            String method = req.getParameter("_method") != null ? req.getParameter("_method").toUpperCase() : ((HttpServletRequest) req).getMethod();
            switch (method) {
                case "GET":
                    if (accessControl.checkUserCanRead(user, req.getRemoteAddr(), device))
                        chain.doFilter(req, resp);
                    else {
                        String msg = String.format("User %s does not have read access to %s", user, device);
                        ((HttpServletResponse) resp).sendError(HttpServletResponse.SC_UNAUTHORIZED, msg);
                        LOG.info(msg);
                    }

                    break;
                case "PUT":
                    if (accessControl.checkUserCanWrite(user, req.getRemoteAddr(), device))
                        chain.doFilter(req, resp);
                    else {
                        String msg = String.format("User %s does not have write access to %s", user, device);
                        ((HttpServletResponse) resp).sendError(HttpServletResponse.SC_UNAUTHORIZED, msg);
                        LOG.info(msg);
                    }
                    break;
                default:
                    ((HttpServletResponse) resp).sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                    LOG.info("Method is not allowed: " + ((HttpServletRequest) req).getMethod());
            }

        } catch (TangoProxyException e) {
            throw new ServletException(e);
        }
    }

    public void init(FilterConfig config) throws ServletException {
        if (config.getServletContext().getAttribute(AccessControl.TANGO_ACCESS) == null)
            throw new ServletException("TangoAccessControl is null");
    }

}
