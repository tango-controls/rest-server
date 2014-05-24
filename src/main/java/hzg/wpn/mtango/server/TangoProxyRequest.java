package hzg.wpn.mtango.server;

import hzg.wpn.mtango.command.CommandInfo;
import hzg.wpn.mtango.util.Json;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author ingvord
 * @since 5/24/14@8:09 PM
 */
public class TangoProxyRequest {
    private static final Pattern uri_pattern = Pattern.compile("/mtango/(.*)/(.*)\\.jsonp?");
    public static final String PARAMETER_CMD = "cmd";

    public final String devname;
    public final String cmd;
    public final String target;


    public TangoProxyRequest(HttpServletRequest value) throws ServletException {
        String uri = value.getRequestURI();
        Matcher m = uri_pattern.matcher(uri);
        if (!m.matches())
            throw new ServletException("uri does not match pattern: /mtango/<devname>/<attr or cmd>.json(p)");

        this.devname = m.group(1);
        this.target = m.group(2);

        this.cmd = value.getParameter(PARAMETER_CMD);
        if (cmd == null) throw new ServletException("cmd is not defined");

    }

    public CommandInfo toCommandInfo() {
        return Json.GSON.fromJson(this.cmd, CommandInfo.class);
    }
}
