package org.tango.web.server.event;

import org.tango.client.ez.proxy.EventData;
import org.tango.client.ez.proxy.TangoEventListener;

import javax.ws.rs.sse.OutboundSseEvent;
import javax.ws.rs.sse.Sse;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11/8/18
 */
public class SseTangoEventListener implements TangoEventListener<Object> {
    public static final int RECONNECTION_DELAY = 30000;
    private final TangoSseBroadcaster broadcaster;
    private final Sse sse;
    private final int eventId;

    public SseTangoEventListener(TangoSseBroadcaster broadcaster, Sse sse, int eventId) {
        this.broadcaster = broadcaster;
        this.sse = sse;
        this.eventId = eventId;
    }

    @Override
    public void onEvent(EventData<Object> data) {
        OutboundSseEvent event = sse.newEventBuilder().
                id(Long.toString(data.getTime())).
                name(Integer.toString(eventId)).
                data(String.valueOf(data.getValue())).
                reconnectDelay(RECONNECTION_DELAY).
                build();

        broadcaster.broadcast(event);
    }

    @Override
    public void onError(Exception cause) {
        OutboundSseEvent event = sse.newEventBuilder().
                id(Long.toString(System.currentTimeMillis())).
                name(Integer.toString(eventId)).
                data("error: " + cause.getMessage()).
                reconnectDelay(RECONNECTION_DELAY).
                build();

        broadcaster.broadcast(event);

    }
}
