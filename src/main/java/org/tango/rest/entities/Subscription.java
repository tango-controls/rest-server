package org.tango.rest.entities;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.tango.web.server.event.Event;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.sse.SseEventSink;
import java.util.List;
import java.util.Optional;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11/7/18
 */
@Path("/{id}")
public class Subscription {
    public final int id;
    public final List<Event> events;
    public final List<Failure> failures;
    private transient SseEventSink sink = null;

    public Subscription(int id, List<Event> events, List<Failure> failures) {
        this.id = id;
        this.events = events;
        this.failures = failures;
    }


    @GET
    public Subscription get(){
        return this;
    }

    @GET
    @Path("/event-stream")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public void getSubscription(@Context SseEventSink sink){
        this.sink = sink;
        events.forEach(event -> event.broadcaster.register(this.sink));
    }

    @PUT
    public Subscription put(){
        return this;
    }

    @JsonIgnore
    public Optional<SseEventSink> getSink(){
        return Optional.ofNullable(sink);
    }
}
