package hzg.wpn.mtango.server;

import fr.esrf.TangoDs.TangoConst;
import hzg.wpn.mtango.DeviceMapper;
import hzg.wpn.tango.client.proxy.EventData;
import hzg.wpn.tango.client.proxy.TangoEvent;
import hzg.wpn.tango.client.proxy.TangoEventCallback;
import hzg.wpn.tango.client.proxy.TangoProxy;
import hzg.wpn.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author ingvord
 * @since 5/25/14@12:55 PM
 */
public class EventsServlet extends HttpServlet {
    public static final Pattern URI_PATTERN;

    private static final Logger LOG = LoggerFactory.getLogger(EventsServlet.class);

    static {
        URI_PATTERN = Pattern.compile("/mtango/events/(" + Arrays.join(TangoConst.eventNames, "|") + ")/result\\.jsonp?");
    }

    private DeviceMapper mapper;

    @Override
    public void init() throws ServletException {
        this.mapper = (DeviceMapper) getServletContext().getAttribute(Launcher.TANGO_MAPPER);
    }

    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        try {
            final CountDownLatch latch = new CountDownLatch(1);
            EventsRequest eventsRequest = new EventsRequest(request);

            TangoProxy proxy = mapper.map(eventsRequest.devname);

            final AtomicReference<EventData<Object>> result = new AtomicReference<>();

            TangoEvent event = TangoEvent.valueOf(eventsRequest.type.toUpperCase());
            //TODO prevent memory leak
            int evtId = proxy.subscribeEvent(eventsRequest.attrname, event, new TangoEventCallback<Object>() {
                @Override
                public void onEvent(EventData<Object> data) {
                    try {
                        result.set(data);
                    } finally {
                        latch.countDown();
                    }
                }

                @Override
                public void onError(Throwable cause) {
                    LOG.error("TangoEvent#onError", cause);

                    try {
//                        Responses.sendFailure(cause, response.getWriter());
//                    } catch (IOException e) {
//                        LOG.error("Can not send response!", e);
                    } finally {
                        latch.countDown();
                    }
                }
            });
            latch.await();
            Responses.sendSuccess(result.get().getValue(), response.getWriter());
        } catch (Exception e) {
            LOG.error("Request processing has failed!", e);

            Responses.sendFailure(e, response.getWriter());
        }
    }

    public static class EventsRequest {


        public String type;
        public String devname;
        public String attrname;

        public EventsRequest(HttpServletRequest req) throws ServletException {
            Matcher m = URI_PATTERN.matcher(req.getRequestURI());
            if (!m.matches()) throw new ServletException("URI does not match pattern: " + URI_PATTERN.toString());

            type = m.group(1);
            if (type == null) throw new ServletException("evt is not defined!");

            devname = req.getParameter("dev");
            if (devname == null) throw new ServletException("dev is not defined!");

            attrname = req.getParameter("attr");
            if (attrname == null) throw new ServletException("attr is not defined!");
        }
    }
}
