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

package org.tango.subscriptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tango.rest.v10.event.EventImpl;
import org.tango.web.server.binding.EventSystem;
import org.tango.web.server.event.EventsManager;
import org.tango.web.server.event.SubscriptionsContext;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11/7/18
 */
@Path("/subscriptions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@EventSystem
public class JaxRsSubscriptions {
    private final Logger logger = LoggerFactory.getLogger(JaxRsSubscriptions.class);

    @Context
    public EventsManager manager;
    @Context
    public SubscriptionsContext context;

    @POST
    public JaxRsSubscription createSubscription(List<EventImpl.Target> events){
        JaxRsSubscription jaxRsSubscription = createSubscription();

        jaxRsSubscription.putTargets(events, manager);
        logger.debug("Create Subscription id={}; events={}; failures={}", jaxRsSubscription.getId(), jaxRsSubscription.getEvents(), jaxRsSubscription.getFailures());


        return jaxRsSubscription;
    }

    @POST
    @Consumes(MediaType.WILDCARD)
    public JaxRsSubscription createSubscription(){
        int id = context.getNextId();
        logger.debug("Create Subscription id={}", id);
        JaxRsSubscription jaxRsSubscription = new JaxRsSubscription(id);
        context.addSubscription(jaxRsSubscription);
        return jaxRsSubscription;
    }


    @Path("/{id}")
    public JaxRsSubscription getSubscription(@PathParam("id") int id){
        return context.getSubscription(id);
    }

}
