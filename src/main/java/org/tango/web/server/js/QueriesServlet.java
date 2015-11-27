package org.tango.web.server.js;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tango.client.ez.proxy.TangoProxy;
import org.tango.web.server.Responses;
import org.tango.web.server.DeviceMapper;
import org.tango.web.server.command.Command;
import org.tango.web.server.command.CommandInfo;
import org.tango.web.server.command.Commands;
import org.tango.web.server.util.Json;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This servlet serves attribute read/write and command execution service
 *
 * Deprecated use {@link org.tango.rest.mtango.MtangoImpl} instead
 *
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11.10.12
 */
@Deprecated
public class QueriesServlet extends HttpServlet {
    public static final Pattern URI_PATTERN = Pattern.compile("/mtango/queries/(read|write|exec)/result\\.jsonp?");
    private static Logger LOG = LoggerFactory.getLogger(QueriesServlet.class);


    private DeviceMapper mapper;

    @Override
    public void init() throws ServletException {
        mapper = (DeviceMapper) getServletContext().getAttribute(DeviceMapper.TANGO_MAPPER);
    }

    @Override
    protected final void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            QueriesRequest request = new QueriesRequest(req);
            CommandInfo commandInfo = request.cmd;
            LOG.info("message received:" + commandInfo.toString());

            TangoProxy proxy = mapper.map(commandInfo.devname);
            Command cmd = Commands.createCommand(commandInfo, proxy);
            Object result = Commands.execute(cmd);

            Responses.sendSuccess(result, resp.getWriter());
        } catch (Exception e) {
            LOG.error("Request processing has failed!", e);

            Responses.sendFailure(e, resp.getWriter());
        }
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

}
