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

package org.tango.web.server.event;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import fr.esrf.Tango.DevState;
import org.tango.client.ez.proxy.EventData;
import org.tango.client.ez.proxy.TangoEventListener;

import javax.ws.rs.sse.OutboundSseEvent;
import javax.ws.rs.sse.Sse;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11/8/18
 */
public class SseTangoEventListener implements TangoEventListener<Object> {
    public static final int RECONNECTION_DELAY = 30000;
    private final TangoSseBroadcaster broadcaster;
    private final Sse sse;
    private final int eventId;

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(DevState.class, (JsonSerializer<DevState>) (src, typeOfSrc, context) -> new JsonPrimitive(src.toString()))
            .create();

    public SseTangoEventListener(TangoSseBroadcaster broadcaster, Sse sse, int eventId) {
        this.broadcaster = broadcaster;
        this.sse = sse;
        this.eventId = eventId;
    }

    @Override
    public void onEvent(EventData<Object> data) {
        OutboundSseEvent event = sse.newEventBuilder().
                id(Long.toString(data.getTime())).
                name(Integer.toString(eventId)).
                data(gson.toJson(data.getValue())).
                reconnectDelay(RECONNECTION_DELAY).
                build();

        broadcaster.broadcast(event);
    }

    @Override
    public void onError(Exception cause) {
        OutboundSseEvent event = sse.newEventBuilder().
                id(Long.toString(System.currentTimeMillis())).
                name(Integer.toString(eventId)).
                data("error: " + cause.getClass().getSimpleName() + ":" + cause.getMessage()).
                reconnectDelay(RECONNECTION_DELAY).
                build();

        broadcaster.broadcast(event);

    }
}
