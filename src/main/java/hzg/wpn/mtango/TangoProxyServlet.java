package hzg.wpn.mtango;

import com.google.common.base.Objects;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import hzg.wpn.mtango.command.Command;
import hzg.wpn.mtango.command.CommandFactory;
import hzg.wpn.mtango.command.CommandInfo;
import hzg.wpn.mtango.command.Result;
import hzg.wpn.util.base64.Base64InputStream;
import org.apache.log4j.Logger;
import wpn.hdri.tango.proxy.TangoProxyException;
import wpn.hdri.tango.proxy.TangoProxyWrapper;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Subclasses are generated
 *
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11.10.12
 */
public class TangoProxyServlet extends HttpServlet {
    private static Logger LOG = Logger.getLogger(TangoProxyServlet.class);


    private String tangoDevice;
    private String tangoHost;
    private String tangoUrl;

    private TangoProxyWrapper proxy;

    private final TangoProxyWrapperFactory factory;
    private final CommandFactory cmdFactory;

    private final Gson gson = new GsonBuilder()
            .serializeNulls()
            .registerTypeAdapter(CommandInfo.class,CommandInfo.jsonDeserializer())
            .create();

    public TangoProxyServlet(TangoProxyWrapperFactory factory, CommandFactory cmdFactory) {
        this.factory = factory;
        this.cmdFactory = cmdFactory;
    }

    //TODO plugable factories

    public TangoProxyServlet() {
        this(new TangoProxyWrapperFactory(), new CommandFactory());
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        this.tangoHost = config.getInitParameter("tango-host");
        this.tangoDevice = config.getInitParameter("tango-device");

        this.tangoUrl = "tango://" + this.tangoHost + "/" + this.tangoDevice;

        try {
            this.proxy = factory.createTangoProxyWrapper(this.tangoUrl);
        } catch (TangoProxyException e) {
            throw new ServletException("Can not create TangoProxyServlet.",e);
        }
    }


    @Override
    protected final void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        CommandInfo commandInfo = transformRequest(req);
        LOG.info("message received:" + commandInfo.toString());
        try {
            Command cmd = cmdFactory.createCommand(commandInfo,proxy);

            Object result = cmd.execute();

            sendResponse(Result.createSuccessResult(result),req,resp);
        } catch (Exception e) {
            Result error = Result.createFailureResult(e.getMessage());

            sendResponse(error,req,resp);
        }
    }

    private CommandInfo transformRequest(HttpServletRequest req) {
        String encoded = req.getParameter("cmd");

        String json = new String(Base64InputStream.decode(encoded)).trim();

        return gson.fromJson(json,CommandInfo.class);
    }

    private void sendResponse(Result result, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
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
