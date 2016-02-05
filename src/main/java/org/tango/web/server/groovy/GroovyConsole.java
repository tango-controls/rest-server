package org.tango.web.server.groovy;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;
import org.codehaus.groovy.control.CompilationFailedException;
import org.tango.web.server.TangoContext;

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
public class GroovyConsole extends HttpServlet {

    public static final String GROOVY_VM = "groovy.vm";
    private GroovyShell groovyShell;

    @Override
    public void init() throws ServletException {
        Binding binding = new Binding();

        binding.setProperty("context", getServletContext().getAttribute(TangoContext.TANGO_CONTEXT));

        groovyShell = new GroovyShell(getClass().getClassLoader(), binding);

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
                result = String.valueOf(groovyShell.run(source, "dynamic.groovy", new String[0]));
            } catch (CompilationFailedException e) {
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
