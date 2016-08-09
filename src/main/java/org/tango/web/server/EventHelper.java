package org.tango.web.server;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import org.tango.client.ez.attribute.Quality;
import org.tango.client.ez.proxy.*;
import org.tango.rest.response.Response;
import org.tango.rest.response.Responses;

import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 20.02.2015
 */
@ThreadSafe
public class EventHelper {
    public static final long CAPACITY = 1000L;//TODO parameter
    private static final ConcurrentMap<String, EventHelper> event_helpers = new ConcurrentLinkedHashMap.Builder<String, EventHelper>()
            .maximumWeightedCapacity(CAPACITY)
            .build();
    //TODO parameters
    private static final long MAX_AWAIT = 30000L;
    private static final long DELTA = 256L;

    public static Response<?> handleEvent(String member, long timeout, State state, TangoProxy proxy,
                                     TangoEvent event) throws TangoProxyException, InterruptedException, NoSuchAttributeException {
        String eventKey = proxy.getName() + "/" + member + "." + event.name();
        if (state == State.INITIAL) {
            EventHelper helper;
            helper = new EventHelper(member, event, proxy);
            EventHelper oldHelper = event_helpers.putIfAbsent(eventKey, helper);
            if (oldHelper == null) {
                //read initial value from the proxy
                ValueTimeQuality<?> attrTimeQuality = proxy.readAttributeValueTimeQuality(member);
                Response<?> result = Responses.createAttributeSuccessResult(
                        attrTimeQuality.getValue(),
                        attrTimeQuality.getTime(),
                        attrTimeQuality.getQuality().name()
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
        EventHelper helper = new EventHelper(member,event,proxy);
        helper.subscribe();
        //block this servlet until event or timeout
        return helper.get(timeout);
        }
    }

    public static enum State {
        UNDEFINED,
        INITIAL,
        CONTINUATION
    }

    private final String attribute;
    private final TangoEvent evt;
    private final TangoProxy proxy;

    private volatile Response<?> value;

    private final Object guard = new Object();
    public EventHelper(String attribute, TangoEvent evt, TangoProxy proxy) {
        this.attribute = attribute;
        this.evt = evt;
        this.proxy = proxy;
    }

    public void set(Response<?> value) {
        synchronized (guard){
            this.value = value;
            guard.notifyAll();
        }
    }

    public boolean hasValue(){
        return value != null;
    }

    public Response<?> get(){
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
    public Response<?> get(long timeout) throws InterruptedException{
        synchronized (guard){
            do
                guard.wait((timeout = timeout - DELTA) > 0 ? timeout : MAX_AWAIT);
            while (value == null);
        }
        return value;
    }

    private TangoEventListener<Object> listener;
    public void subscribe() throws TangoProxyException, NoSuchAttributeException {
        proxy.subscribeToEvent(attribute, evt);

        listener = new TangoEventListener<Object>() {
            @Override
            public void onEvent(EventData<Object> data) {
                EventHelper.this.set(Responses.createAttributeSuccessResult(data.getValue(), data.getTime(), Quality.VALID.name()));
            }

            @Override
            public void onError(Exception cause) {
                EventHelper.this.set(Responses.createFailureResult(cause));
            }
        };
        proxy.addEventListener(attribute, evt, listener);
    }
}
