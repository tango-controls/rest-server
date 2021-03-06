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

package org.tango.subscriptions;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.tango.client.ez.proxy.NoSuchAttributeException;
import org.tango.client.ez.proxy.TangoProxyException;
import org.tango.rest.entities.Failures;
import org.tango.rest.rc4.entities.Failure;
import org.tango.rest.v10.event.Event;
import org.tango.rest.v10.event.Subscription;
import org.tango.web.server.event.EventImpl;
import org.tango.web.server.event.EventsManager;
import org.tango.web.server.event.SubscriptionsContext;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.sse.SseEventSink;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11/7/18
 */
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class JaxRsSubscription {
    private final Subscription subscription;
    private transient SseEventSink sink = null;

    public JaxRsSubscription(int id) {
        this.subscription = new Subscription(id);
    }


    public int getId(){
        return subscription.id;
    }

    public List<EventImpl> getEvents(){
        return (List<EventImpl>) subscription.events;
    }

    public List<Failure> getFailures(){
        return subscription.failures;
    }

    @GET
    public Subscription get(){
        return subscription;
    }

    @GET
    @Path("/event-stream")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public void getSubscription(@Context SubscriptionsContext context,@Context SseEventSink sink){
        this.sink = new org.tango.web.server.event.SseEventSink(sink);
        getEvents().forEach(event -> event.broadcaster.register(this.sink));
        context.getMaintenanceBroadcaster().register(this.sink);
    }

    @PUT
    public List<EventImpl> putTargets(List<EventImpl.Target> targets, @Context EventsManager manager){
        List<EventImpl> list = targets.stream()
                .map(target ->
                        manager.lookupEvent(target).orElseGet(() -> newEventWrapper(manager, target, subscription.failures)))
                .filter(Objects::nonNull)
                .filter(event -> !this.getEvents().contains(event))
                .collect(Collectors.toList());
        this.getEvents().addAll(list);

        getSink().ifPresent(sseEventSink -> list.forEach(event -> event.broadcaster.register(sseEventSink)));
        return list;
    }

    @DELETE
    public JaxRsSubscription deleteTargets(List<Event.Target> targets){
        List<EventImpl> toRemove = getEvents().stream().filter(event -> targets.contains(event.target)).collect(Collectors.toList());

        getEvents().removeAll(toRemove);
        cancel(toRemove);

        return this;
    }


    @DELETE
    @Consumes(MediaType.WILDCARD)
    public void delete(@Context SubscriptionsContext context){
        JaxRsSubscription jaxRsSubscription = context.removeSubscription(subscription.id);
        if(jaxRsSubscription != this) throw new AssertionError("Trying to delete invalid Subscription!");
        jaxRsSubscription.cancel(jaxRsSubscription.getEvents());
        jaxRsSubscription.getSink().ifPresent(SseEventSink::close);
    }


    private EventImpl newEventWrapper(EventsManager manager, EventImpl.Target target, List<Failure> failures) {
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

    public void cancel(List<EventImpl> events) {
        getSink().ifPresent(
                sseEventSink -> events.forEach(event -> event.broadcaster.deregister(sseEventSink)));
    }
}
