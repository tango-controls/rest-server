package org.tango.web.server.event;

import org.tango.client.ez.proxy.TangoEventListener;

import java.util.Objects;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11/8/18
 */
public class Event {
    public final int id;
    public final Target target;

    private transient final TangoEventListener<Object> tangoEventListener;
    private transient final TangoSseBroadcaster broadcaster;

    public Event(int id, Target target, TangoEventListener<Object> tangoEventListener, TangoSseBroadcaster broadcaster) {
        this.id = id;
        this.target = target;
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

    public static class Target {
        public final String host;
        public final String device;
        public final String attribute;
        public final String type;
        private final String string;

        public Target(String host, String device, String attribute, String type) {
            this.host = host;
            this.device = device;
            this.attribute = attribute;
            this.type = type;

            this.string = host + "/" + device + "/" + attribute + "/" + type;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Target target = (Target) o;
            return Objects.equals(host, target.host) &&
                    Objects.equals(device, target.device) &&
                    Objects.equals(attribute, target.attribute) &&
                    Objects.equals(type, target.type);
        }

        @Override
        public int hashCode() {
            return Objects.hash(host, device, attribute, type);
        }

        @Override
        public String toString() {
            return string;
        }
    }
}
