package org.tango.web.server.event;

import org.tango.client.ez.proxy.TangoEventListener;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11/8/18
 */
public class EventsManager {
    ConcurrentMap<Event.Target, Event> events = new ConcurrentHashMap<>();
    private final AtomicInteger eventId = new AtomicInteger(0);

    private final TangoSseBroadcasterFactory tangoSseBroadcasterFactory;

    public EventsManager(TangoSseBroadcasterFactory tangoSseBroadcasterFactory) {
        this.tangoSseBroadcasterFactory = tangoSseBroadcasterFactory;
    }

    public Event lookupEvent(String host, String device, String attribute, String type){
        Event.Target target = new Event.Target(host, device, attribute, type);
        return events.get(target);
    }

    public Event initializeEvent(String host, String device, String attribute, String type) throws Exception {
        Event.Target target = new Event.Target(host, device, attribute, type);
        TangoSseBroadcaster broadcaster = tangoSseBroadcasterFactory.newInstance();
        int id = eventId.incrementAndGet();
        Event event = new Event(id, target, new SseTangoEventListener(broadcaster,tangoSseBroadcasterFactory.getSse(),id), broadcaster);
        event = events.putIfAbsent(target, event);
        if(event != null){
            eventId.decrementAndGet();
        }

        return event;
    }

}
