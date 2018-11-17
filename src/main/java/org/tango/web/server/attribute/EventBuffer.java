package org.tango.web.server.attribute;

import fr.esrf.Tango.AttrQuality;
import org.tango.client.ez.proxy.*;
import org.tango.rest.entities.AttributeValue;
import org.tango.rest.entities.Failure;
import org.tango.rest.entities.Failures;

import javax.ws.rs.core.Response;
import java.util.Comparator;
import java.util.NavigableSet;
import java.util.concurrent.*;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11/27/17
 */
public class EventBuffer {
    private static final long KEEP_EVENT_DATA_DELAY = 30_000L;

    public static class EventKey implements Comparable<EventKey> {
        private final String proxy;
        private final String attribute;
        private final TangoEvent event;
        private final String value;

        public EventKey(TangoProxy proxy, String attribute, TangoEvent event) {
            this.proxy = proxy.toDeviceProxy().fullName();
            this.attribute = attribute;
            this.event = event;
            this.value = this.proxy + "/" + attribute + "." + event;
        }

        @Override
        public String toString() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            EventKey eventKey = (EventKey) o;

            return value.equals(eventKey.value);
        }

        @Override
        public int hashCode() {
            return value.hashCode();
        }

        @Override
        public int compareTo(EventKey o) {
            return value.compareTo(o.value);
        }
    }

    public static class Event {
        private final EventKey key;
        private final TangoProxy proxy;
        private final TangoEventListener listener;
        private CompletableFuture<Object> future = new CompletableFuture<>();

        Event(EventKey key, TangoProxy proxy) {
            this.key = key;
            this.proxy = proxy;

            this.listener = new TangoEventListener<Object>() {
                @Override
                public void onEvent(EventData<Object> data) {
                    AttributeValue<Object> value = new AttributeValue<>(key.attribute, null, key.proxy, data.getValue(), AttrQuality.ATTR_VALID.toString(), data.getTime());
                    Event.this.future.complete(value);
                }

                @Override
                public void onError(Exception cause) {
                    Event.this.future.completeExceptionally(cause);
                }
            };
        }

        void subscribe() {
            proxy.addEventListener(key.attribute, key.event, listener);
        }

        public Object get(long timeout) throws InterruptedException {
            try {
                return this.future.get(timeout, TimeUnit.MILLISECONDS);
            } catch (ExecutionException e) {
                return Response.status(Response.Status.BAD_REQUEST).entity(Failures.createInstance(e)).build();
            } catch (TimeoutException e) {
                return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(
                        Failures.createInstance(
                                String.format("There is no %s event from upstream Tango device[%s]. Timeout[%d ms] has expired!", key.event, proxy.getName(), timeout))).build();
            } finally {
                proxy.removeEventListener(key.attribute, key.event, listener);
            }
        }
    }

    private final ConcurrentMap<EventKey, TangoEventListener<?>> eventListeners = new ConcurrentHashMap<>();

    private final ConcurrentMap<EventKey, ConcurrentSkipListSet<Object>> eventData
            = new ConcurrentSkipListMap<>();

    /**
     * Creates new Event instance
     * <p>
     * Subscribes this buffer to a proxy/attribute.event
     *
     * @param key
     * @param proxy
     * @return
     */
    public Event createEvent(EventKey key, TangoProxy proxy) {


        Event newEvent = new Event(key, proxy);
        newEvent.subscribe();
        return newEvent;
    }

    /**
     * Subscribes this buffer to a proxy if and only if the key is not yet subscribed
     *
     * @param key
     * @param proxy
     * @throws TangoProxyException
     * @throws NoSuchAttributeException
     */
    public void subscribe(EventKey key, TangoProxy proxy) throws TangoProxyException, NoSuchAttributeException {
        TangoEventListener<?> listener = null;
        if (!eventListeners.containsKey(key)) {
            listener = new TangoEventListener<Object>() {
                @Override
                public void onEvent(EventData<Object> data) {
                    AttributeValue<Object> value = new AttributeValue<>(key.attribute, null, key.proxy, data.getValue(), AttrQuality.ATTR_VALID.toString(), data.getTime());
                    EventBuffer.this.put(key, value);
                    removeFirst(data.getTime(), eventData.get(key));
                }

                @Override
                public void onError(Exception cause) {
                    Failure value = Failures.createInstance(cause);
                    EventBuffer.this.put(key, value);
                    removeFirst(System.currentTimeMillis(), eventData.get(key));
                }
            };

            TangoEventListener<?> prev = eventListeners.putIfAbsent(key, listener);
            if (prev != null) {
                listener = prev;
            }
        }
        // this case is required due to proxy recreation logic
        // if proxy is a new one we re-add our listener to it
        if (proxy.subscribeToEvent(key.attribute, key.event)) {
            proxy.addEventListener(key.attribute, key.event, listener);
        }
    }

    private void put(EventKey eventKey, Object data) {
        ConcurrentSkipListSet<Object> skipListSet
                = new ConcurrentSkipListSet<>(new EventDataComparator());

        ConcurrentSkipListSet<Object> previousSkipListSet = eventData.putIfAbsent(eventKey, skipListSet);
        if (previousSkipListSet != null) {
            skipListSet = previousSkipListSet;
        }

        skipListSet.add(data);
    }

    private void removeFirst(long timestamp, ConcurrentSkipListSet<Object> skipListSet) {
        //remove first element if it is older than 10s
        Object oldData = skipListSet.first();
        if (getTimestamp(oldData) < timestamp - KEEP_EVENT_DATA_DELAY) skipListSet.remove(oldData);
    }

    public NavigableSet<Object> getTail(EventKey eventKey, long timestamp) {
        return eventData.getOrDefault(
                eventKey, new ConcurrentSkipListSet<>(new EventDataComparator()))
                .tailSet(new AttributeValue(null, null, null, null, null, timestamp), true);
    }

    private static long getTimestamp(Object o1) {
        if (AttributeValue.class.isAssignableFrom(o1.getClass()))
            return ((AttributeValue) o1).timestamp;
        else if (Failure.class.isAssignableFrom(o1.getClass()))
            return ((Failure) o1).timestamp;
        else throw new AssertionError(o1.getClass().getSimpleName());
    }

    private static class EventDataComparator implements Comparator<Object> {
        @Override
        public int compare(Object o1, Object o2) {
            long l1 = getTimestamp(o1);
            long l2 = getTimestamp(o2);

            return Long.compare(l1, l2);
        }
    }
}
