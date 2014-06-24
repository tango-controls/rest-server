package org.tango.web.server.rest;

/**
 * @author Ingvord
 * @since 21.06.14
 */

import fr.esrf.TangoApi.AttributeInfoEx;
import fr.esrf.TangoApi.CommandInfo;
import fr.esrf.TangoApi.DeviceInfo;
import org.apache.commons.beanutils.ConvertUtils;
import org.tango.client.ez.proxy.TangoProxy;
import org.tango.client.ez.proxy.TangoProxyException;
import org.tango.web.server.DatabaseDs;
import org.tango.web.server.DeviceMapper;
import org.tango.web.server.Responses;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

@Path("/")
public class Rest2Tango {
    @GET
    @Path("devices")
    @Produces("application/json")
    //TODO marshaller
    public Collection<String> getDevices(@Context ServletContext ctx) throws Exception {//TODO handle exception
        DatabaseDs db = (DatabaseDs) ctx.getAttribute(DatabaseDs.TANGO_DB);
        return db.getDeviceList();
    }

    @GET
    @Path("device/{domain}/{name}/{instance}")
    @Produces("application/json")
    public DeviceInfo getDeviceInfo(@PathParam("domain") String domain,
                                    @PathParam("name") String name,
                                    @PathParam("instance") String instance,
                                    @Context ServletContext ctx) throws Exception {//TODO exceptions
        DatabaseDs db = (DatabaseDs) ctx.getAttribute(DatabaseDs.TANGO_DB);
        DeviceInfo info = db.getDeviceInfo(domain + "/" + name + "/" + instance);
        return info;
    }

    @GET
    @Path("device/{domain}/{name}/{instance}/attributes")
    @Produces("application/json")
    //TODO marshaller
    public Collection<AttributeInfoEx> getDeviceAttributes(@PathParam("domain") String domain,
                                                           @PathParam("name") String name,
                                                           @PathParam("instance") String instance,
                                                           @Context ServletContext ctx) throws Exception {
        TangoProxy proxy = lookupTangoProxy(domain, name, instance, ctx);
        Collection<AttributeInfoEx> result = new ArrayList<>();
        Collections.addAll(result, proxy.toDeviceProxy().get_attribute_info_ex());
        return result;
    }

    @GET
    @Path("device/{domain}/{name}/{instance}/commands")
    @Produces("application/json")
    //TODO marshaller
    public Collection<CommandInfo> getDeviceCommands(@PathParam("domain") String domain,
                                                     @PathParam("name") String name,
                                                     @PathParam("instance") String instance,
                                                     @Context ServletContext ctx) throws Exception {
        TangoProxy proxy = lookupTangoProxy(domain, name, instance, ctx);
        Collection<CommandInfo> result = new ArrayList<>();
        Collections.addAll(result, proxy.toDeviceProxy().command_list_query());
        return result;

    }

    @PUT
    @Path("device/{domain}/{name}/{instance}/{attr}/argin={arg}")
    @Produces("application/json")
    //TODO marshaller
    public void putAttribute(@PathParam("domain") String domain,
                             @PathParam("name") String name,
                             @PathParam("instance") String instance,
                             @PathParam("attr") String attr,
                             @PathParam("arg") String arg,
                             @Context ServletContext ctx) throws Exception {
        TangoProxy proxy = lookupTangoProxy(domain, name, instance, ctx);
        if (!proxy.hasAttribute(attr))
            throw new IllegalArgumentException(String.format("Device %s does not have attribute %s", proxy.getName(), attr));
        Class<?> targetType = proxy.getAttributeInfo(attr).getClazz();
        Object converted = ConvertUtils.convert(arg, targetType);
        proxy.writeAttribute(attr, converted);
    }

    @GET
    @Path("device/{domain}/{name}/{instance}/{cmd}")
    @Produces("application/json")
    public Object getCommandOrAttribute(@PathParam("domain") String domain,
                                        @PathParam("name") String name,
                                        @PathParam("instance") String instance,
                                        @PathParam("cmd") String cmd,
                                        @Context ServletContext ctx) throws Exception {
        TangoProxy proxy = lookupTangoProxy(domain, name, instance, ctx);
        if (proxy.hasAttribute(cmd))
            return Responses.createSuccessResult(proxy.readAttribute(cmd));
        else
            return Responses.createSuccessResult(proxy.executeCommand(cmd, null));
    }

    @GET
    @Path("device/{domain}/{name}/{instance}/{cmd}/argin={arg}")
    @Produces("application/json")
    public Responses getCommandWithArg(@PathParam("domain") String domain,
                                       @PathParam("name") String name,
                                       @PathParam("instance") String instance,
                                       @PathParam("cmd") String cmd,
                                       @PathParam("arg") String arg,
                                       @Context ServletContext ctx) throws Exception {
        //TODO exceptions
        TangoProxy proxy = lookupTangoProxy(domain, name, instance, ctx);
        if (!proxy.hasCommand(cmd))
            throw new IllegalArgumentException(String.format("Device %s does not have command %s", proxy.getName(), cmd));
        Class<?> targetType = proxy.getCommandInfo(cmd).getArginType();
        if (targetType == Void.class) return proxy.executeCommand(cmd, null);
        Object converted = ConvertUtils.convert(arg, targetType);
        return Responses.createSuccessResult(proxy.executeCommand(cmd, converted));
    }

    private TangoProxy lookupTangoProxy(String domain, String name, String instance, ServletContext ctx) throws TangoProxyException {
        DeviceMapper mapper = (DeviceMapper) ctx.getAttribute(DeviceMapper.TANGO_MAPPER);
        return mapper.map(domain + "/" + name + "/" + instance);
    }
}
