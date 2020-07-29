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

package org.tango.web.server.proxy;

import fr.esrf.Tango.AttrDataFormat;
import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DeviceProxy;
import org.tango.client.ez.proxy.*;
import org.tango.utils.DevFailedUtils;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author ingvord
 * @since 11/18/18
 */
public class TangoAttributeProxyImpl implements TangoAttributeProxy {
    private final TangoDeviceProxy proxy;
    private final String attribute;
    private final AtomicLong lastUpdated = new AtomicLong();

    public TangoAttributeProxyImpl(TangoDeviceProxy deviceProxy, String attribute) {
        this.proxy = deviceProxy;
        this.attribute = attribute;
    }

    @Override
    public void write(Object value) throws DevFailed {
        try {
            proxy.getProxy().writeAttribute(attribute, value);
            lastUpdated.set(System.currentTimeMillis());
        } catch (WriteAttributeException | NoSuchAttributeException e) {
            throw DevFailedUtils.newDevFailed(e);
        }
    }

    @Override
    public <T> T readPlain() throws DevFailed {
        try {
            lastUpdated.set(System.currentTimeMillis());
            return proxy.getProxy().readAttribute(attribute);
        } catch (ReadAttributeException | NoSuchAttributeException e) {
            throw DevFailedUtils.newDevFailed(e);
        }
    }

    @Override
    public <T> ValueTimeQuality<T> read() throws DevFailed {
        try {
            ValueTimeQuality<T> result = proxy.getProxy().readAttributeValueTimeQuality(attribute);
            lastUpdated.set(result.time);
            return result;
        } catch (ReadAttributeException | NoSuchAttributeException e) {
            throw DevFailedUtils.newDevFailed(e);
        }
    }

    @Override
    public boolean isImage() {
        try {
            return proxy.getProxy().getAttributeInfo(attribute).getFormat().toAttrDataFormat() == AttrDataFormat.IMAGE;
        } catch (TangoProxyException | NoSuchAttributeException e) {
            return false;
        }
    }

    @Override
    public DeviceProxy getDeviceProxy() {
        return proxy.getProxy().toDeviceProxy();
    }

    @Override
    public String getName() {
        return attribute;
    }

    @Override
    public long getLastUpdatedTimestamp() {
        return lastUpdated.get();
    }
}
