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

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DeviceProxy;
import org.tango.client.ez.proxy.ExecuteCommandException;
import org.tango.client.ez.proxy.NoSuchCommandException;
import org.tango.client.ez.proxy.TangoProxy;
import org.tango.utils.DevFailedUtils;

/**
 * @author ingvord
 * @since 11/18/18
 */
public class TangoCommandProxyImpl implements TangoCommandProxy {
    private final TangoProxy proxy;
    private final String cmdName;

    public TangoCommandProxyImpl(TangoProxy proxy, String cmdName) {
        this.proxy = proxy;
        this.cmdName = cmdName;
    }

    @Override
    public String getCommandName() {
        return cmdName;
    }

    @Override
    public void execute(Object value) throws DevFailed {
        executeExtract(value);
    }

    @Override
    public Object executeExtract(Object value) throws DevFailed {
        try {
            return proxy.executeCommand(cmdName, value);
        } catch (ExecuteCommandException | NoSuchCommandException e) {
            throw DevFailedUtils.newDevFailed(e);
        }
    }

    @Override
    public DeviceProxy getDeviceProxy() {
        return proxy.toDeviceProxy();
    }
}
