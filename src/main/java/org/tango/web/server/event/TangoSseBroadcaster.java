package org.tango.web.server.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.sse.OutboundSseEvent;
import javax.ws.rs.sse.SseBroadcaster;
import javax.ws.rs.sse.SseEventSink;
import java.lang.reflect.Field;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11/8/18
 */
public class TangoSseBroadcaster implements SseBroadcaster {
    private final Logger logger = LoggerFactory.getLogger(TangoSseBroadcaster.class);

    private final AtomicInteger registeredSinks = new AtomicInteger(0);

    private final SseBroadcaster broadcaster;
    private final ConcurrentLinkedQueue<SseEventSink> outputQueue;
    private final AtomicBoolean closed;

    public TangoSseBroadcaster(SseBroadcaster broadcaster) {
        this.broadcaster = broadcaster;
        try {
            Field fld = this.broadcaster.getClass().getDeclaredField("outputQueue");
            fld.setAccessible(true);
            this.outputQueue = (ConcurrentLinkedQueue<SseEventSink>) fld.get(this.broadcaster);
            fld = this.broadcaster.getClass().getDeclaredField("closed");
            fld.setAccessible(true);
            this.closed = (AtomicBoolean) fld.get(this.broadcaster);

        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new AssertionError("Failed to extract fields from SseBroadcaster!");
        }

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
        logger.debug("Registered sinks: {}", registeredSinks.get());
    }

    /**
     * Removes the sink from broadcasting. If there is no more sinks to broadcast close this broadcaster.
     *
     * @param sseEventSink
     */
    public void deregister(SseEventSink sseEventSink){
        logger.trace("Deregister SseEventSink {}", sseEventSink);
        //TODO protect with write lock from broadcaster?
        int decrementAndGet = registeredSinks.decrementAndGet();
        logger.debug("Registered sinks: {}", decrementAndGet);
    }

    @Override
    public CompletionStage<?> broadcast(OutboundSseEvent event) {
        logger.trace("Broadcasting event {}", event);
        return broadcaster.broadcast(event);
    }

    @Override
    public void close() {
        logger.trace("Closing this broadcaster");
        broadcaster.close();
    }

    public boolean isClosed(){
        return closed.get();
    }

    public int size() {
        return registeredSinks.get();
    }
}
