package org.tango.rest;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import fr.esrf.Tango.AttrQuality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tango.client.ez.proxy.*;
import org.tango.rest.entities.AttributeValue;
import org.tango.rest.entities.Failures;

import javax.annotation.concurrent.ThreadSafe;
import javax.ws.rs.core.Response;
import java.util.concurrent.ConcurrentMap;

/**
 * //TODO transform into resource
 *
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 20.02.2015
 */
@ThreadSafe
public class Event {
    public static final long CAPACITY = 1000L;//TODO parameter
    private static final ConcurrentMap<String, Event> event_helpers = new ConcurrentLinkedHashMap.Builder<String, Event>()
            .maximumWeightedCapacity(CAPACITY)
            .build();
    //TODO parameters
    private static final long MAX_AWAIT = 30000L;
    private static final long DELTA = 256L;
    private final Logger logger = LoggerFactory.getLogger(Event.class);
    private final String attribute;
    private final TangoEvent evt;
    private final TangoProxy proxy;
    private final Object guard = new Object();
    private volatile Object value;
    private TangoEventListener<Object> listener;

    public Event(String attribute, TangoEvent evt, TangoProxy proxy) {
        this.attribute = attribute;
        this.evt = evt;
        this.proxy = proxy;
    }

    public static Object handleEvent(final String member, long timeout, State state, TangoProxy proxy,
                                     TangoEvent event) throws TangoProxyException, InterruptedException, NoSuchAttributeException {
        String eventKey = proxy.getName() + "/" + member + "." + event.name();
        if (state == State.INITIAL) {
            Event helper;
            helper = new Event(member, event, proxy);
            Event oldHelper = event_helpers.putIfAbsent(eventKey, helper);
            if (oldHelper == null) {
                //read initial value from the proxy
                ValueTimeQuality<?> attrTimeQuality = proxy.readAttributeValueTimeQuality(member);
                AttributeValue<?> result = new AttributeValue<Object>(
                        member,
                        attrTimeQuality.value,
                        attrTimeQuality.quality.toString(),
                        attrTimeQuality.time
                );

                helper.set(result);
                helper.subscribe();
                return result;
            } else {
                //block this servlet until event or timeout if it has no value
                return oldHelper.hasValue() ? oldHelper.get() : oldHelper.get(timeout);
            }
        } else if(state == State.CONTINUATION){
            //block this servlet until event or timeout
            //TODO could throw NPE if cached value is garbage collected
            return event_helpers.get(eventKey).get(timeout);
        } else {
            Event helper = new Event(member, event, proxy);
        helper.subscribe();
        //block this servlet until event or timeout
        return helper.get(timeout);
        }
    }

    public void set(Object value) {
        synchronized (guard){
            this.value = value;
            guard.notifyAll();
        }
    }

    public boolean hasValue(){
        return value != null;
    }

    public Object get() {
        return value;
    }

    /**
     * Waits for value to be set
     *
     * If value is not set before timeout then returns stored value
     *
     * @param timeout
     * @return
     * @throws InterruptedException
     */
    public Object get(long timeout) throws InterruptedException {
        try {
            synchronized (guard) {
                guard.wait(timeout);
            }
            return value == null ?
                    Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(Failures.createInstance("value has not been updated")).build()
                    : value;
        } finally {
            proxy.removeEventListener(attribute, evt, listener);
        }
    }

    public void subscribe() throws TangoProxyException, NoSuchAttributeException {
        proxy.subscribeToEvent(attribute, evt);

        listener = new TangoEventListener<Object>() {
            @Override
            public void onEvent(EventData<Object> data) {
                logger.debug(this + "#onEvent");
                Event.this.set(new AttributeValue<Object>(attribute, data.getValue(), AttrQuality.ATTR_VALID.toString(), data.getTime()));
            }

            @Override
            public void onError(Exception cause) {
                logger.debug(this + "#onError ", cause);
                Event.this.set(Failures.createInstance(cause));
            }
        };
        proxy.addEventListener(attribute, evt, listener);
    }

    public static enum State {
        UNDEFINED,
        INITIAL,
        CONTINUATION
    }
}
