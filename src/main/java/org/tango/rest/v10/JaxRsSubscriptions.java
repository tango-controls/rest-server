package org.tango.rest.v10;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tango.rest.v10.event.EventImpl;
import org.tango.web.server.binding.EventSystem;
import org.tango.web.server.event.EventsManager;
import org.tango.web.server.event.SubscriptionsContext;

import javax.ws.rs.*;
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

    private final EventsManager manager;
    private final SubscriptionsContext context;

    public JaxRsSubscriptions(EventsManager manager, SubscriptionsContext context) {
        this.manager = manager;
        this.context = context;
    }


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
