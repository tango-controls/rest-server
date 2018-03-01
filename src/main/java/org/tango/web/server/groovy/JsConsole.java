package org.tango.web.server.groovy;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;
import org.tango.TangoRestServer;

import javax.script.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.util.Properties;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 2/5/16
 */
public class JsConsole extends HttpServlet {

    private static final String GROOVY_VM = "groovy.vm";
    private ScriptEngine engine;
    private Bindings bindings;

    @Override
    public void init() throws ServletException {
        bindings = new SimpleBindings();

        bindings.put("context", getServletContext().getAttribute(TangoRestServer.class.getName()));

        engine = new ScriptEngineManager().getEngineByName("nashorn");

        Properties p = new Properties();
        p.setProperty("file.resource.loader.path", getServletContext().getRealPath("/"));
        Velocity.init(p);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String source = req.getParameter("source");
        if(source == null) source = "";

        String result;

        if(source.isEmpty()){
            result = "";
        } else {
            try {
                result = String.valueOf(engine.eval(source, bindings));
            } catch (ScriptException e) {
                throw new ServletException(e);
            }
        }



        Writer out = resp.getWriter();

        Context vctx = new VelocityContext();

        vctx.put("uri", req.getRequestURI());

        vctx.put("text", source);
        vctx.put("result", result);

        Velocity.mergeTemplate(GROOVY_VM, "UTF-8", vctx, out);
    }
}
