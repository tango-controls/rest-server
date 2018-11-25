package org.tango.rest.v10.event;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.tango.client.ez.proxy.*;
import org.tango.web.server.event.TangoSseBroadcaster;

import java.util.Objects;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11/8/18
 */
public class EventImpl extends Event {
    @JsonIgnore
    public final TangoProxy proxy;

    public transient final TangoEventListener<Object> tangoEventListener;
    public transient final TangoSseBroadcaster broadcaster;

    public EventImpl(int id, Target target, TangoProxy proxy, TangoEventListener<Object> tangoEventListener, TangoSseBroadcaster broadcaster) {
        super(id, target);
        this.proxy = proxy;
        this.tangoEventListener = tangoEventListener;
        this.broadcaster = broadcaster;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Event event = (Event) o;
        return id == event.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public void initialize() throws NoSuchAttributeException, TangoProxyException {
        TangoEvent tangoEvent = TangoEvent.valueOf(target.type.toUpperCase());
        proxy.subscribeToEvent(target.attribute, tangoEvent);

        proxy.addEventListener(target.attribute, tangoEvent, tangoEventListener);
    }

}
