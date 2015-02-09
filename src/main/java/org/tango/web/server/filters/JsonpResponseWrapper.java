package org.tango.web.server.filters;

import org.tango.web.rest.Responses;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
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

        resp.setContentType("application/javascript");

        PrintWriter out = new PrintWriter(resp.getOutputStream());

        try {
            out.append(";").append(callback).append("(");
            out.flush();
            chain.doFilter(req, resp);
            out.append(");");
            out.flush();
            //this is the last filter in the chain
            resp.flushBuffer();
        } catch (Exception e) {
            Responses.sendFailure(e, out);
        }
    }

    public void init(FilterConfig config) throws ServletException {

    }

}
