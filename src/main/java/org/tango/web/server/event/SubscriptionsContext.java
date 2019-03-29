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

import org.tango.subscriptions.JaxRsSubscription;

import javax.ws.rs.sse.OutboundSseEvent;
import javax.ws.rs.sse.SseEventSink;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11/9/18
 */
public class SubscriptionsContext {
    public static final long SUBSCRIPTIONS_MAINTENANCE_DELAY = 30L;
    private final ConcurrentMap<Integer, JaxRsSubscription> subscriptions = new ConcurrentHashMap<>();
    private final AtomicInteger id = new AtomicInteger(0);

    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2, new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "event-system-subscriptions-maintenance-thread");
        }
    });

    private static final SseEventSink DUMMY_SINK = new SseEventSink() {
        @Override
        public boolean isClosed() {
            return true;
        }

        @Override
        public CompletionStage<?> send(OutboundSseEvent event) {
            throw new UnsupportedOperationException("This method is not supported in " + this.getClass());
        }

        @Override
        public void close() {
            throw new UnsupportedOperationException("This method is not supported in " + this.getClass());
        }
    };

    public SubscriptionsContext() {
        this.scheduler.scheduleAtFixedRate(() -> {
            subscriptions.entrySet().removeIf(integerSubscriptionEntry ->
                    integerSubscriptionEntry.getValue().getSink().orElse(DUMMY_SINK).isClosed()
            );
        }, SUBSCRIPTIONS_MAINTENANCE_DELAY,SUBSCRIPTIONS_MAINTENANCE_DELAY,TimeUnit.MINUTES);
    }

    public int getNextId(){
        return id.incrementAndGet();
    }

    public void addSubscription(JaxRsSubscription jaxRsSubscription){
        subscriptions.put(jaxRsSubscription.getId(), jaxRsSubscription);
    }

    public JaxRsSubscription getSubscription(int id){
        return subscriptions.get(id);
    }

    public JaxRsSubscription removeSubscription(int id) {
        return subscriptions.remove(id);
    }
}
