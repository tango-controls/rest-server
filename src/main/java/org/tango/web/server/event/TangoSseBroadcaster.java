package org.tango.web.server.event;

import javax.ws.rs.sse.OutboundSseEvent;
import javax.ws.rs.sse.SseBroadcaster;
import javax.ws.rs.sse.SseEventSink;
import java.lang.reflect.Field;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11/8/18
 */
public class TangoSseBroadcaster implements SseBroadcaster {
    private final AtomicInteger registeredSinks = new AtomicInteger(0);

    private final SseBroadcaster broadcaster;
    private final ConcurrentLinkedQueue<SseEventSink> outputQueue;

    public TangoSseBroadcaster(SseBroadcaster broadcaster) throws Exception {
        this.broadcaster = broadcaster;
        Field fld = this.broadcaster.getClass().getDeclaredField("outputQueue");
        this.outputQueue = (ConcurrentLinkedQueue<SseEventSink>) fld.get(this.broadcaster);

        this.broadcaster.onError((sseEventSink, throwable) -> TangoSseBroadcaster.this.deregister(sseEventSink));
    }



    @Override
    public void onError(BiConsumer<SseEventSink, Throwable> onError) {
        broadcaster.onError(onError);
    }

    @Override
    public void onClose(Consumer<SseEventSink> onClose) {
        broadcaster.onClose(onClose);
    }

    @Override
    public void register(SseEventSink sseEventSink) {
        broadcaster.register(sseEventSink);
        registeredSinks.incrementAndGet();
    }

    /**
     * Removes the sink from broadcasting. If there is no more sinks to broadcast close this broadcaster.
     *
     * @param sseEventSink
     */
    public void deregister(SseEventSink sseEventSink){
        //TODO protect with write lock from broadcaster?
        if(outputQueue.remove(sseEventSink) &&
                registeredSinks.decrementAndGet() == 0)
            this.close();
    }

    @Override
    public CompletionStage<?> broadcast(OutboundSseEvent event) {
        return broadcaster.broadcast(event);
    }

    @Override
    public void close() {
        broadcaster.close();
    }
}
