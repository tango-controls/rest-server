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

package org.tango.web.server.interceptors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * Wraps response according to JsonP requirement, i.e.
 * <p/>
 * <code>
 * <cbk>(<json>);
 * </code>
 * <p/>
 * Where cbk - is the name of the js function passed in the request; json - output of the filtered servlet (obviously must be a json)
 *
 * @author Ingord
 * @since 5/24/14@12:04 PM
 */
@Provider
public class JsonpResponseWrapper implements WriterInterceptor {
    private final Logger logger = LoggerFactory.getLogger(JsonpResponseWrapper.class);

    @Context
    private UriInfo uriInfo;

    @Override
    public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {
        logger.trace("Method : aroundWriteTo");
        String callback = getCallbackQueryParameter();

        if (callback == null) {
            context.proceed();
            return;
        }

        OutputStream outputStream = context.getOutputStream();

        PrintWriter out = new PrintWriter(outputStream);
        out.append(";").append(callback).append("(");
        out.flush();
        context.proceed();
        out.append(");");
        out.flush();
    }

    public String getCallbackQueryParameter() {
        MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
        String callback = null;
        if (queryParameters.containsKey("cbk")) {//JMVC
            callback = queryParameters.getFirst("cbk");
        } else if (queryParameters.containsKey("jsonp")) {//Webix
            callback = queryParameters.getFirst("jsonp");
        } else if (queryParameters.containsKey("callback")) {//jQuery
            callback = queryParameters.getFirst("callback");
        }
        return callback;
    }
}
