package org.tango.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tango.rest.entities.Subscription;
import org.tango.web.server.binding.EventSystem;
import org.tango.web.server.event.Event;
import org.tango.web.server.event.EventsManager;
import org.tango.web.server.event.SubscriptionsContext;
import zmq.Sub;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.sse.SseEventSink;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11/7/18
 */
@Path("/subscriptions")
@Produces(MediaType.APPLICATION_JSON)
@EventSystem
public class Subscriptions {
    private final Logger logger = LoggerFactory.getLogger(Subscriptions.class);

    private final EventsManager manager;
    private final SubscriptionsContext context;

    public Subscriptions(EventsManager manager, SubscriptionsContext context) {
        this.manager = manager;
        this.context = context;
    }


    @POST
    public Subscription createSubscription(List<Event.Target> events){
        int id = context.getNextId();
        logger.debug("Create Subscription id={}", id);
        Subscription subscription = new Subscription(id);

        subscription.putTargets(events, manager);
        logger.debug("Create Subscription id={}; events={}; failures={}", id, subscription.events, subscription.failures);
        context.addSubscription(subscription);

        return subscription;
    }



    @Path("/{id}")
    public Subscription getSubscription(@PathParam("id") int id){
        return context.getSubscription(id);
    }

    @DELETE
    @Path("/{id}")
    public void deleteSubscription(@PathParam("id") int id){
        Subscription subscription = context.removeSubscription(id);
        logger.debug("Delete Subscription id={}", id);
        subscription.cancel(subscription.events);
        subscription.getSink().ifPresent(SseEventSink::close);
    }
}
