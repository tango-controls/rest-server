package hzg.wpn.mtango.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import hzg.wpn.mtango.command.Command;
import hzg.wpn.mtango.command.CommandInfo;
import hzg.wpn.mtango.command.Commands;
import hzg.wpn.mtango.command.Result;
import hzg.wpn.mtango.server.filters.TangoDeviceMapper;
import hzg.wpn.tango.client.proxy.TangoProxy;
import hzg.wpn.util.base64.Base64InputStream;
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

    private final Gson gson = new GsonBuilder()
            .serializeNulls()
            .registerTypeAdapter(CommandInfo.class, CommandInfo.jsonDeserializer())
            .create();

    @Override
    protected final void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        CommandInfo commandInfo = extractCommandInfo(req);
        LOG.info("message received:" + commandInfo.toString());
        TangoProxy proxy = (TangoProxy) req.getAttribute(TangoDeviceMapper.ATTR_TANGO_PROXY);
        final Command cmd = Commands.createCommand(commandInfo, proxy);
        try {
            Object result = Commands.execute(cmd);

            sendResponse(Result.createSuccessResult(result), req, resp);
        } catch (Exception e) {
            LOG.error("Request processing has failed!", e);

            Result error = Result.createFailureResult(createExceptionMessage(e));

            sendResponse(error, req, resp);
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

    private CommandInfo extractCommandInfo(HttpServletRequest req) {
        String encoded = req.getParameter("cmd");

        String json = new String(Base64InputStream.decode(encoded)).trim();

        return gson.fromJson(json, CommandInfo.class);
    }

    private void sendResponse(Result result, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/javascript");
        String callback = req.getParameter("callback");
        if (callback == null) {
            throw new ServletException("callback parameter can not be null");
        }

        gson.toJson(result, resp.getWriter());
    }

    @Override
    protected final void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        throw new ServletException(new UnsupportedOperationException());
    }

    //TODO do other verbs
}
