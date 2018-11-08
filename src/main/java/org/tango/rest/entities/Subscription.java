package org.tango.rest.entities;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.sse.SseEventSink;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11/7/18
 */
@Path("/{id}")
public class Subscription {
    private final int id;
    private final AtomicBoolean fallbackToPolling = new AtomicBoolean(true);
    private final ConcurrentLinkedQueue<Event> events = new ConcurrentLinkedQueue<>();
    private final Object sinkGuard = new Object();
    private SseEventSink sink = null;

    public Subscription(int id, List<Event> events) {
        this.id = id;
        this.events.addAll(events);
    }


    @GET
    public Subscription get(){
        return this;
    }

    @GET
    @Path("/event-stream")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public void getSubscription(@Context SseEventSink sink){
        synchronized (sinkGuard) {
            if(this.sink == null)
                this.sink = sink;
        }
    }

    @PUT
    public Subscription put(){
        return this;
    }

    public static class Event {
        private int id;
        public String host;
        public String device;
        public String attribute;
        public String type;
        public int rate;
    }
}
