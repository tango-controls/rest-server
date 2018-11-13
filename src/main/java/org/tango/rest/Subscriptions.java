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
@Consumes(MediaType.APPLICATION_JSON)
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
        Subscription subscription = createSubscription();

        subscription.putTargets(events, manager);
        logger.debug("Create Subscription id={}; events={}; failures={}", subscription.id, subscription.events, subscription.failures);


        return subscription;
    }

    @POST
    @Consumes(MediaType.WILDCARD)
    public Subscription createSubscription(){
        int id = context.getNextId();
        logger.debug("Create Subscription id={}", id);
        Subscription subscription = new Subscription(id);
        context.addSubscription(subscription);
        return subscription;
    }



    @Path("/{id}")
    public Subscription getSubscription(@PathParam("id") int id){
        return context.getSubscription(id);
    }

}