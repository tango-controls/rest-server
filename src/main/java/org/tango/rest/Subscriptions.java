package org.tango.rest;

import org.tango.rest.entities.Subscription;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.sse.SseEventSink;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11/7/18
 */
@Path("/subscriptions")
@Produces(MediaType.APPLICATION_JSON)
public class Subscriptions {
    private final ConcurrentMap<Integer, Subscription> subscriptions = new ConcurrentHashMap<>();
    private final AtomicInteger counter  = new AtomicInteger(0);


    @POST
    public Subscription createSubscription(List<Subscription.Event> events){
        return new Subscription(counter.getAndIncrement(), events);
    }

    @Path("/{id}")
    public Subscription getSubscription(@PathParam("id") Integer id){
        return subscriptions.get(id);
    }


}
