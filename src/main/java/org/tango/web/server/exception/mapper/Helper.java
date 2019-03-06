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

package org.tango.web.server.exception.mapper;

import fr.esrf.Tango.DevFailed;
import org.tango.client.ez.proxy.TangoProxyException;
import org.tango.rest.rc4.entities.Failures;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 4/18/17
 */
public class Helper {
    private Helper() {
    }

    public static Response getResponse(DevFailed exception, Response.Status status) {
        return Response.status(status).entity(Failures.createInstance(exception)).type(MediaType.APPLICATION_JSON).build();
    }

    public static Response getResponse(Exception exception, Response.Status status) {
        return Response.status(status).entity(Failures.createInstance(exception)).type(MediaType.APPLICATION_JSON).build();
    }

    public static Response getResponse(TangoProxyException exception, Response.Status status) {
        return Response.status(status).entity(Failures.createInstance(exception)).type(MediaType.APPLICATION_JSON).build();
    }
}
