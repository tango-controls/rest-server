package hzg.wpn.mtango.server;

import hzg.wpn.mtango.DatabaseDs;
import hzg.wpn.mtango.command.Command;
import hzg.wpn.mtango.command.CommandInfo;
import hzg.wpn.mtango.command.Commands;
import hzg.wpn.mtango.util.Json;
import hzg.wpn.tango.client.attribute.Quality;
import org.javatuples.Triplet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Subclasses are generated
 *
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11.10.12
 */
public class QueriesServlet extends HttpServlet {
    public static final Pattern URI_PATTERN = Pattern.compile("/mtango/queries/(read|write|exec)/result.jsonp?");
    private static Logger LOG = LoggerFactory.getLogger(QueriesServlet.class);


    private DeviceMapper mapper;

    @Override
    public void init() throws ServletException {
        DatabaseDs db = (DatabaseDs) getServletContext().getAttribute(Launcher.TANGO_DB);
        mapper = new DeviceMapper(db);
    }

    @Override
    protected final void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            QueriesRequest request = new QueriesRequest(req);
            CommandInfo commandInfo = request.cmd;
            LOG.info("message received:" + commandInfo.toString());

            hzg.wpn.tango.client.proxy.TangoProxy proxy = mapper.map(commandInfo.devname);
            Command cmd = Commands.createCommand(commandInfo, proxy);
            Object result = Commands.execute(cmd);

            QueriesResponse.sendSuccess(result, resp.getWriter());
        } catch (Exception e) {
            LOG.error("Request processing has failed!", e);

            QueriesResponse.sendFailure(createExceptionMessage(e), resp.getWriter());
        }
    }

    private String[] createExceptionMessage(Throwable e) {
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

    /**
     * @author ingvord
     * @since 5/24/14@8:09 PM
     */
    public static class QueriesRequest {
        public static final String PARAMETER_CMD = "cmd";

        private String query;
        private CommandInfo cmd;


        public QueriesRequest(HttpServletRequest value) throws ServletException {
            String uri = value.getRequestURI();
            Matcher m = URI_PATTERN.matcher(uri);
            if (!m.matches())
                throw new ServletException("uri does not match pattern: " + URI_PATTERN.toString());

            this.query = m.group(1);

            String cmd = value.getParameter(PARAMETER_CMD);
            if (cmd == null) throw new ServletException("query is not defined");
            this.cmd = Json.GSON.fromJson(cmd, CommandInfo.class);
            this.cmd.type = this.query;
        }
    }

    /**
     * @author ingvord
     * @since 5/24/14@8:10 PM
     */
    public static class QueriesResponse {
        private final Object argout;
        private final String[] error;
        private final Quality quality;
        private final long timestamp;

        private QueriesResponse(Object argout, String[] error, Quality quality, long timestamp) {
            this.argout = argout;
            this.error = error;
            this.quality = quality;
            this.timestamp = timestamp;
        }

        public static void sendSuccess(Object argout, Writer out) {
            QueriesResponse resp = createSuccessResult(argout);
            Json.GSON.toJson(resp, out);
        }

        public static void sendFailure(String[] errors, Writer out) {
            QueriesResponse resp = QueriesResponse.createFailureResult(errors);
            Json.GSON.toJson(resp, out);
        }

        public static QueriesResponse createSuccessResult(Object argout) {
            if (argout != null && Triplet.class.isAssignableFrom(argout.getClass())) {
                Triplet<Object, Long, Quality> triplet = (Triplet<Object, Long, Quality>) argout;
                return new QueriesResponse(triplet.getValue0(), null, triplet.getValue2(), triplet.getValue1());
            } else {
                return new QueriesResponse(argout, null, Quality.VALID, System.currentTimeMillis());
            }
        }

        public static QueriesResponse createFailureResult(String[] message) {
            if (message == null || message.length == 0)
                message = new String[]{"Unexpected server side error! See server log for details."};
            return new QueriesResponse(null, message, Quality.INVALID, System.currentTimeMillis());
        }
    }
}
