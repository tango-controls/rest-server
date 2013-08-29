package hzg.wpn.mtango;

import com.google.common.base.Objects;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import hzg.wpn.mtango.command.Command;
import hzg.wpn.mtango.command.CommandFactory;
import hzg.wpn.mtango.command.CommandInfo;
import hzg.wpn.mtango.command.Result;
import hzg.wpn.util.base64.Base64InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wpn.hdri.tango.proxy.TangoProxyException;
import wpn.hdri.tango.proxy.TangoProxyWrapper;
import wpn.hdri.tango.proxy.TangoProxyWrappers;

import javax.servlet.ServletConfig;
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

    private String tangoDevice;
    private String tangoHost;
    private String tangoUrl;

    private TangoProxyWrapper proxy;
    private CommandExecutionStrategy commandExecutionStrategy;

    private final CommandFactory cmdFactory;
    private final CommandExecutionStrategyFactory commandExecutionStrategyFactory;

    private final Gson gson = new GsonBuilder()
            .serializeNulls()
            .registerTypeAdapter(CommandInfo.class, CommandInfo.jsonDeserializer())
            .create();

    public TangoProxyServlet(CommandFactory cmdFactory, CommandExecutionStrategyFactory commandExecutionStrategyFactory) {
        this.cmdFactory = cmdFactory;
        this.commandExecutionStrategyFactory = commandExecutionStrategyFactory;
    }

    public TangoProxyServlet() {
        this(new CommandFactory(), new CommandExecutionStrategyFactory());
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        this.tangoHost = config.getInitParameter("tango-host");
        this.tangoDevice = config.getInitParameter("tango-device");

        this.tangoUrl = "tango://" + this.tangoHost + "/" + this.tangoDevice;

        try {
            this.proxy = TangoProxyWrappers.newInstance(this.tangoUrl);
        } catch (TangoProxyException e) {
            LOG.error("Can not create TangoProxyServlet.", e);
            throw new ServletException("Can not create TangoProxyServlet.", e);
        }

        commandExecutionStrategy = commandExecutionStrategyFactory.createCommandExecutionStrategy(config.getInitParameter("keep-result"));
    }


    @Override
    protected final void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        CommandInfo commandInfo = extractCommandInfo(req);
        LOG.info("message received:" + commandInfo.toString());
        final Command cmd = cmdFactory.createCommand(commandInfo, proxy);
        try {
            Object result = commandExecutionStrategy.execute(cmd);

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
        //due to based on reflection implementation. See CommandImpl
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

        String jsonData = gson.toJson(result);
        StringBuilder output = new StringBuilder();

        output.append(callback).append("(").append(jsonData).append(");");
        resp.getWriter().write(output.toString());
    }

    @Override
    protected final void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        throw new ServletException(new UnsupportedOperationException());
    }

    //TODO do other verbs

    public String getTangoDevice() {
        return tangoDevice;
    }

    public String getTangoHost() {
        return tangoHost;
    }

    public String getTangoUrl() {
        return tangoUrl;
    }

    protected TangoProxyWrapper getProxy() {
        return proxy;
    }

    @Override
    public final String toString() {
        return Objects.toStringHelper(this).addValue(tangoUrl).toString();
    }
}
