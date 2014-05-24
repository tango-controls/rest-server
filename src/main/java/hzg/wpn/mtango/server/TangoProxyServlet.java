package hzg.wpn.mtango.server;

import hzg.wpn.mtango.DatabaseDs;
import hzg.wpn.mtango.command.Command;
import hzg.wpn.mtango.command.CommandInfo;
import hzg.wpn.mtango.command.Commands;
import hzg.wpn.tango.client.proxy.TangoProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Subclasses are generated
 *
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11.10.12
 */
public class TangoProxyServlet extends HttpServlet {
    private static Logger LOG = LoggerFactory.getLogger(TangoProxyServlet.class);


    private DeviceMapper mapper;

    @Override
    public void init() throws ServletException {
        DatabaseDs db = (DatabaseDs) getServletContext().getAttribute(TangoProxyLauncher.TANGO_DB);
        mapper = new DeviceMapper(db);
    }

    @Override
    protected final void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            TangoProxyRequest request = new TangoProxyRequest(req);
            CommandInfo commandInfo = request.toCommandInfo();
            LOG.info("message received:" + commandInfo.toString());

            TangoProxy proxy = mapper.map(request.devname);
            final Command cmd = Commands.createCommand(commandInfo, proxy);
            Object result = Commands.execute(cmd);

            TangoProxyResponse.sendSuccess(result, resp.getWriter());
        } catch (Exception e) {
            LOG.error("Request processing has failed!", e);

            TangoProxyResponse.sendFailure(createExceptionMessage(e), resp.getWriter());
        }
    }

    private String[] createExceptionMessage(Throwable e) {
        //TODO avoid temporary collection creation
        Set<String> result = new LinkedHashSet<String>();
        //skip upper exception because it is always TargetInvocationException
        //due to based on reflection implementation. See Command
        e = e.getCause();
        do {
            if (e.getMessage() != null)
                result.add(e.getMessage());
        }
        while ((e = e.getCause()) != null);
        return result.toArray(new String[result.size()]);
    }

    @Override
    protected final void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        throw new ServletException(new UnsupportedOperationException());
    }

    //TODO do other verbs
}
