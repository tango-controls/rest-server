package org.tango.web.server.filters;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Wraps response according to JsonP requirement, i.e.
 * <p/>
 * <code>
 * <cbk>(<json>);
 * </code>
 * <p/>
 * Where cbk - is the name of the js function passed in the request; json - output of the filtered servlet (obviously must be a json)
 *
 * @author Ingord
 * @since 5/24/14@12:04 PM
 */
public class JsonpResponseWrapper implements Filter {
    public void destroy() {
    }

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws ServletException, IOException {
        String callback = req.getParameter("cbk");
        if (callback == null) throw new ServletException("cbk parameter is not defined in the request!");
        ((HttpServletResponse)resp).setDateHeader("Expires", System.currentTimeMillis() + Long.MAX_VALUE); // jsonp never expires
        resp.setContentType("application/javascript");

        PrintWriter out = new PrintWriter(resp.getOutputStream());

        out.append(";").append(callback).append("(");
        out.flush();
        try {
            chain.doFilter(req, resp);
        } catch (Exception e) {
            //Responses.sendFailure(e, out);
        }
        out.append(");");
        out.flush();
        //this is the last filter in the chain
        resp.flushBuffer();
    }

    public void init(FilterConfig config) throws ServletException {

    }

}
