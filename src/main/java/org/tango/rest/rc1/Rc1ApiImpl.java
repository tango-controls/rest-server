package org.tango.rest.rc1;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.AttributeInfoEx;
import fr.esrf.TangoApi.CommandInfo;
import fr.esrf.TangoApi.DeviceAttribute;
import org.jboss.resteasy.annotations.cache.Cache;
import org.tango.client.ez.data.type.TangoDataType;
import org.tango.client.ez.proxy.TangoProxy;
import org.tango.client.ez.proxy.TangoProxyException;
import org.tango.client.ez.util.TangoUtils;
import org.tango.web.server.DatabaseDs;
import org.tango.web.server.DeviceMapper;
import org.tango.web.server.Responses;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.Arrays;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 27.11.2015
 */
@Path("/rc1")
@Produces("application/json")
public class Rc1ApiImpl {
    @GET
    @Cache(maxAge = 10)
    @Path("devices")
    public Object devices(@QueryParam("wildcard") String wildcard,
                            @Context final ServletContext context) {
        DatabaseDs db = (DatabaseDs) context.getAttribute(DatabaseDs.TANGO_DB);

        try {
            Iterable<String> result = db.getDeviceList(wildcard == null ? "*" : wildcard);
            Iterable<Object> transform = Iterables.transform(result, new Function<String, Object>() {
                @Override
                public Object apply(final String input) {
                    return new Object() {
                        public final String name = input;
                        public final String href = context.getContextPath() + "/rest/rc1/devices/" + input;
                    };
                }
            });
            return transform;
        } catch (TangoProxyException e) {
            return Response.ok(Responses.createFailureResult(e)).build();
        }
    }

    @GET
    @Cache(maxAge = 10)
    @Path("devices/{domain}/{family}/{member}")
    public Object device(@PathParam("domain") String domain,
                         @PathParam("family") String family,
                         @PathParam("member") String member,
                         @Context final ServletContext context) {
        try {
            DatabaseDs db = (DatabaseDs) context.getAttribute(DatabaseDs.TANGO_DB);
            final TangoProxy proxy = DeviceMapper.lookup(domain, family, member, context);
            final String href = context.getContextPath() + "/rest/rc1/" + proxy.getName();
            return new Device(proxy.getName(), db.getDeviceInfo(proxy.getName()),
                    Collections2.transform(Arrays.asList(proxy.toDeviceProxy().get_attribute_info_ex()), new Function<AttributeInfoEx, NamedEntity>() {
                        @Override
                        public NamedEntity apply(AttributeInfoEx input) {
                            return new NamedEntity(input.name, href + "/attributes/" + input.name);
                        }
                    }),
                    Collections2.transform(Arrays.asList(proxy.toDeviceProxy().command_list_query()), new Function<CommandInfo, NamedEntity>() {
                        @Override
                        public NamedEntity apply(CommandInfo input) {
                            return new NamedEntity(input.cmd_name, href + "/commands/" + input.cmd_name);
                        }
                    }),
                    Collections2.transform(Arrays.asList(proxy.toDeviceProxy().get_property_list("*")), new Function<String, NamedEntity>() {
                        @Override
                        public NamedEntity apply(String input) {
                            return new NamedEntity(input, href + "/properties/" + input);
                        }
                    }), href);
        } catch (DevFailed devFailed){
            return Responses.createFailureResult(TangoUtils.convertDevFailedToException(devFailed));
        }catch (TangoProxyException e) {
            return Responses.createFailureResult(e);
        }
    }

    @GET
    @Path("devices/{domain}/{family}/{member}/state")
    public Object deviceState(@PathParam("domain") String domain,
                              @PathParam("family") String family,
                              @PathParam("member") String member,
                              @Context ServletContext context){
        try {
            final TangoProxy proxy = DeviceMapper.lookup(domain, family, member, context);
            final String href = context.getContextPath() + "/rest/rc1/" + proxy.getName();
            final DeviceAttribute[] ss = proxy.toDeviceProxy().read_attribute(new String[]{"State","Status"});
            Object result = new Object(){
                public final String state = ss[0].extractDevState().toString();
                public final String status = ss[1].extractString();
                public final Object _links = new Object(){
                    public final String _state = href + "/State";
                    public final String _status = href + "/Status";
                    public final String _parent = href;
                    public final String _self = href + "/state";
                };
            };

            return result;
        } catch (TangoProxyException e) {
            return Responses.createFailureResult(e);
        } catch (DevFailed devFailed) {
            return Responses.createFailureResult(TangoUtils.convertDevFailedToException(devFailed));
        }
    }
}
