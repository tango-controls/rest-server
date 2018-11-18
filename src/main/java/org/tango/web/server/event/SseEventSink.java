package org.tango.web.server.event;

import javax.ws.rs.sse.OutboundSseEvent;
import java.util.concurrent.CompletionStage;

/**
 * Mimics {@link org.jboss.resteasy.plugins.providers.sse.SseEventInputImpl} to prevent removing first sink from {@link org.jboss.resteasy.plugins.providers.sse.SseBroadcasterImpl#outputQueue}
 * at {@link org.jboss.resteasy.plugins.providers.sse.SseBroadcasterImpl#87} due to {@link javax.ws.rs.core.GenericType#equals(Object)}
 *
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11/12/18
 */
public class SseEventSink implements javax.ws.rs.sse.SseEventSink {
    private final javax.ws.rs.sse.SseEventSink impl;

    public SseEventSink(javax.ws.rs.sse.SseEventSink impl) {
        this.impl = impl;
    }

    @Override
    public boolean isClosed() {
        return impl.isClosed();
    }

    @Override
    public CompletionStage<?> send(OutboundSseEvent event) {
        return impl.send(event);
    }

    @Override
    public void close() {
        impl.close();
    }
}
