package org.tango.rest;

import org.tango.client.ez.proxy.NoSuchAttributeException;
import org.tango.client.ez.proxy.TangoProxyException;
import org.tango.rest.entities.Failure;
import org.tango.rest.entities.Failures;
import org.tango.rest.entities.Subscription;
import org.tango.web.server.binding.EventSystem;
import org.tango.web.server.event.Event;
import org.tango.web.server.event.EventsManager;
import org.tango.web.server.event.SubscriptionsContext;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
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

    private final EventsManager manager;
    private final SubscriptionsContext context;

    public Subscriptions(EventsManager manager, SubscriptionsContext context) {
        this.manager = manager;
        this.context = context;
    }


    @POST
    public Subscription createSubscription(List<Event.Target> events){
        List<Failure> failures = new ArrayList<>();

        List<Event> list = events.stream()
                .map(target ->
                        manager.lookupEvent(target).orElseGet(() -> newEventWrapper(manager, target, failures)))
                .collect(Collectors.toList());

        int id = context.getNextId();
        Subscription subscription = new Subscription(id, list, failures);


        context.addSubscription(subscription);

        return subscription;
    }

    private Event newEventWrapper(EventsManager manager, Event.Target target, List<Failure> failures) {
        try {
            return manager.newEvent(target);
        } catch (TangoProxyException|NoSuchAttributeException e) {
            failures.add(Failures.createInstance(e));
            return null;
        }
    }

    @Path("/{id}")
    public Subscription getSubscription(@PathParam("id") int id){
        return context.getSubscription(id);
    }


}
