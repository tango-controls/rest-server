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

import org.codehaus.jackson.annotate.JsonIgnore;
import org.tango.client.ez.proxy.*;
import org.tango.rest.v10.event.Event;

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
