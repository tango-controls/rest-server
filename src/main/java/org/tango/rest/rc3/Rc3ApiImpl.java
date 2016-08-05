package org.tango.rest.rc3;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import fr.esrf.Tango.AttributeConfig;
import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.AttributeInfo;
import fr.esrf.TangoApi.DbAttribute;
import fr.esrf.TangoApi.PipeBlob;
import org.tango.client.ez.proxy.NoSuchCommandException;
import org.tango.client.ez.proxy.TangoProxy;
import org.tango.client.ez.proxy.TangoProxyException;
import org.tango.rest.SupportedAuthentication;
import org.tango.rest.entities.NamedEntity;
import org.tango.rest.rc2.Rc2ApiImpl;
import org.tango.rest.response.Responses;
import org.tango.web.server.DatabaseDs;
import org.tango.web.server.EventHelper;
import org.tango.web.server.Launcher;
import org.tango.web.server.TangoContext;
import org.tango.web.server.providers.Partitionable;
import org.tango.web.server.providers.StaticValue;
import org.tango.web.server.providers.TangoDatabaseBackend;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 8/5/16
 */
@Path("/rc3")
@Produces("application/json")
public class Rc3ApiImpl extends Rc2ApiImpl {
    public static String REST_PREFIX = "/rest/rc3";


    @GET
    public Map<String, String> authentication(@Context ServletContext context, @Context TangoContext tangoContext) {
        Map<String, String> result = new HashMap<>();

        String tangoHost = tangoContext.tangoHost;
        result.put(tangoHost, context.getContextPath() + REST_PREFIX + "/" + tangoHost.replace(':', '/'));
        result.put("x-auth-method", SupportedAuthentication.VALUE);

        return result;
    }

    @GET
    @Partitionable
    @StaticValue
    @Path("/{host}/{port}")
    public Object database(@Context final DatabaseDs db,
                           @Context final ServletContext context) throws Exception {
        final String[] tangoHost = db.toDeviceProxy().get_tango_host().split(":");
        return new Object(){
            public String name = db.toDeviceProxy().get_name();
            public String host = tangoHost[0];
            public int port = Integer.parseInt(tangoHost[1]);
            public List<String> info = db.getInfo();
            public String devices = uriInfo.getAbsolutePath() + "/devices";
        };
    }

    @GET
    @Partitionable
    @StaticValue
    @Path("/{host}/{port}/devices")
    public Object devices(@QueryParam("wildcard") String wildcard,
                          @Context DatabaseDs db,
                          @Context final ServletContext context) {
        return super.devices(wildcard, db, context);
    }

    @GET
    @StaticValue
    @Path("/{host}/{port}/devices/{domain}/{family}/{member}")
    public Object device(@Context TangoProxy proxy, @Context DatabaseDs db, @Context ServletContext context) {
        return super.device(proxy, db, context);
    }

    @GET
    @Partitionable
    @StaticValue
    @Path("/{host}/{port}/devices/{domain}/{family}/{member}/attributes")
    public Object deviceAttributes(@Context TangoProxy proxy, @Context ServletContext context) throws Exception {
        return super.deviceAttributes(proxy, context);
    }


    @GET
    @StaticValue
    @Path("/{host}/{port}/devices/{domain}/{family}/{member}/attributes/{attr}")
    public Object deviceAttribute(@PathParam("attr") String attrName, @Context UriInfo uriInfo, @Context TangoProxy proxy, @Context ServletContext context) throws Exception {
        return super.deviceAttribute(attrName, uriInfo, proxy, context);
    }

    @Override
    public Object deviceAttributeEvent(String domain, String family, String member, String attrName, String event, long timeout, EventHelper.State state, @Context ServletContext context, @Context UriInfo uriInfo) throws InterruptedException, URISyntaxException {
        return super.deviceAttributeEvent(domain, family, member, attrName, event, timeout, state, context, uriInfo);
    }

    @Override
    public Object deviceAttributeHistory(String attrName, @Context TangoProxy proxy) throws DevFailed {
        return super.deviceAttributeHistory(attrName, proxy);
    }

    @Override
    public Object deviceAttributeHistory(String attrName, @Context UriInfo uriInfo, @Context TangoProxy proxy, @Context ServletContext context) throws Exception {
        return super.deviceAttributeHistory(attrName, uriInfo, proxy, context);
    }

    @Override
    public AttributeInfo deviceAttributeInfo(String attrName, @Context TangoProxy proxy) throws DevFailed {
        return super.deviceAttributeInfo(attrName, proxy);
    }

    @Override
    public AttributeInfo deviceAttributeInfoPut(String attrName, boolean async, @Context TangoProxy proxy, AttributeConfig config) throws DevFailed {
        return super.deviceAttributeInfoPut(attrName, async, proxy, config);
    }

    @Override
    public DbAttribute deviceAttributeProperties(String attrName, @Context TangoProxy proxy) throws DevFailed {
        return super.deviceAttributeProperties(attrName, proxy);
    }

    @Override
    public void deviceAttributePropertyDelete(String attrName, String propName, @Context TangoProxy proxy) throws DevFailed {
        super.deviceAttributePropertyDelete(attrName, propName, proxy);
    }

    @Override
    public DbAttribute deviceAttributePropertyPut(String attrName, String propName, String propValue, boolean async, @Context TangoProxy proxy) throws DevFailed {
        return super.deviceAttributePropertyPut(attrName, propName, propValue, async, proxy);
    }


    @Override
    public Object deviceAttributesPut(@Context TangoProxy proxy, @Context UriInfo uriInfo, @Context ServletContext context, @Context HttpServletRequest request) throws DevFailed {
        return super.deviceAttributesPut(proxy, uriInfo, context, request);
    }

    @Override
    public Object deviceAttributeValueGet(String attrName, @Context TangoProxy proxy) throws Exception {
        return super.deviceAttributeValueGet(attrName, proxy);
    }

    @Override
    public Object deviceAttributeValuePut(String attrName, String value, boolean async, @Context TangoProxy proxy) throws Exception {
        return super.deviceAttributeValuePut(attrName, value, async, proxy);
    }

    @Override
    public Object deviceCommand(String cmdName, @Context TangoProxy proxy, @Context UriInfo uriInfo) throws DevFailed {
        return super.deviceCommand(cmdName, proxy, uriInfo);
    }

    @Override
    public Object deviceCommandHistory(String cmdName, @Context TangoProxy proxy, @Context UriInfo uriInfo) throws DevFailed {
        return super.deviceCommandHistory(cmdName, proxy, uriInfo);
    }

    @Override
    public Object deviceCommandPut(String cmdName, String[] value, boolean async, @Context TangoProxy proxy, @Context UriInfo uriInfo) throws Exception {
        return super.deviceCommandPut(cmdName, value, async, proxy, uriInfo);
    }

    @Override
    public Object deviceCommands(@Context TangoProxy proxy, @Context UriInfo uriInfo) throws DevFailed {
        return super.deviceCommands(proxy, uriInfo);
    }

    @Override
    public Object devicePipeGet(String pipeName, @Context UriInfo uriInfo, @Context TangoProxy proxy) throws DevFailed {
        return super.devicePipeGet(pipeName, uriInfo, proxy);
    }

    @Override
    public Object devicePipePut(String pipeName, boolean async, @Context UriInfo info, @Context TangoProxy proxy, PipeBlob blob) throws DevFailed {
        return super.devicePipePut(pipeName, async, info, proxy, blob);
    }

    @Override
    public Object devicePipes(@Context UriInfo uriInfo, @Context TangoProxy proxy) throws DevFailed {
        return super.devicePipes(uriInfo, proxy);
    }

    @Override
    public Object deviceProperties(@Context TangoProxy proxy) throws DevFailed {
        return super.deviceProperties(proxy);
    }

    @Override
    public Object devicePropertiesPost(@Context HttpServletRequest request, @Context TangoProxy proxy) throws DevFailed {
        return super.devicePropertiesPost(request, proxy);
    }

    @Override
    public Object devicePropertiesPut(@Context HttpServletRequest request, @Context TangoProxy proxy) throws DevFailed {
        return super.devicePropertiesPut(request, proxy);
    }

    @Override
    public Object deviceProperty(String propName, @Context TangoProxy proxy) throws DevFailed {
        return super.deviceProperty(propName, proxy);
    }

    @Override
    public void devicePropertyDelete(String propName, @Context HttpServletRequest request, @Context TangoProxy proxy) throws DevFailed {
        super.devicePropertyDelete(propName, request, proxy);
    }

    @Override
    public Object devicePropertyPost(String propName, @Context HttpServletRequest request, @Context TangoProxy proxy) throws DevFailed {
        return super.devicePropertyPost(propName, request, proxy);
    }

    @Override
    public Object devicePropertyPut(String propName, @Context HttpServletRequest request, @Context TangoProxy proxy) throws DevFailed {
        return super.devicePropertyPut(propName, request, proxy);
    }

    @Override
    public Object deviceState(@Context TangoProxy proxy, @Context ServletContext context) {
        return super.deviceState(proxy, context);
    }
}
