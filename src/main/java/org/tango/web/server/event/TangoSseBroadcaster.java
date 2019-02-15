/*
 * Copyright 2019 Tango Controls
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tango.web.server.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.sse.OutboundSseEvent;
import javax.ws.rs.sse.SseBroadcaster;
import javax.ws.rs.sse.SseEventSink;
import java.lang.reflect.Field;
import java.util.IdentityHashMap;
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

    private final AtomicInteger registeredSinksCounter = new AtomicInteger(0);

    private final SseBroadcaster broadcaster;
    private final ConcurrentLinkedQueue<SseEventSink> outputQueue;
    private final AtomicBoolean closed;
    private final IdentityHashMap<SseEventSink, Void> registeredSinks = new IdentityHashMap<>();

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

        this.broadcaster.onClose((sseEventSink) -> {
            logger.debug("onClose {}, outputQueue {}",sseEventSink.hashCode(), outputQueue.size());
            //we do not need to remove sink as it was removed in original broadcaster
            if(registeredSinks.containsKey(sseEventSink)){
                deregister(sseEventSink);
            }
        });
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
        registeredSinks.put(sseEventSink, null);
        registeredSinksCounter.incrementAndGet();
        logger.debug("Registered sinks: {}", registeredSinksCounter.get());
    }

    /**
     * Removes the sink from broadcasting. If there is no more sinks to broadcast close this broadcaster.
     *
     * @param sseEventSink
     */
    public void deregister(SseEventSink sseEventSink){
        outputQueue.remove(sseEventSink);
        registeredSinks.remove(sseEventSink);
        logger.debug("Deregister SseEventSink. Registered sinks: {}", registeredSinksCounter.decrementAndGet());
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
        return registeredSinksCounter.get();
    }
}
