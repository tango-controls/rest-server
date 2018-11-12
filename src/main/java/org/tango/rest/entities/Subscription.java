package org.tango.rest.entities;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.tango.client.ez.proxy.NoSuchAttributeException;
import org.tango.client.ez.proxy.TangoProxyException;
import org.tango.web.server.event.Event;
import org.tango.web.server.event.EventsManager;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.sse.SseEventSink;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11/7/18
 */
@Path("/{id}")
public class Subscription {
    public final int id;
    public final List<Event> events = new ArrayList<>();
    public final List<Failure> failures = new ArrayList<>();
    private transient SseEventSink sink = null;

    public Subscription(int id) {
        this.id = id;
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
    public Subscription putTargets(List<Event.Target> targets, @Context EventsManager manager){
        List<Event> list = targets.stream()
                .map(target ->
                        manager.lookupEvent(target).orElseGet(() -> newEventWrapper(manager, target, failures)))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        this.events.addAll(list);

        return this;
    }

    @DELETE
    public Subscription deleteTargets(List<Event.Target> targets){
        List<Event> toRemove = this.events.stream().filter(event -> targets.contains(event.target)).collect(Collectors.toList());

        this.events.removeAll(toRemove);
        cancel(toRemove);

        return this;
    }



    private Event newEventWrapper(EventsManager manager, Event.Target target, List<Failure> failures) {
        try {
            return manager.newEvent(target);
        } catch (TangoProxyException | NoSuchAttributeException e) {
            failures.add(Failures.createInstance(e));
            return null;
        }
    }

    @JsonIgnore
    public Optional<SseEventSink> getSink(){
        return Optional.ofNullable(sink);
    }

    public void cancel(List<Event> events) {
        getSink().ifPresent(
                sseEventSink -> events.forEach(event -> event.broadcaster.deregister(sseEventSink)));
    }
}
