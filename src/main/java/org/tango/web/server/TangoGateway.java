package org.tango.web.server;

/**
 * @author Ingvord
 * @since 21.06.14
 */

import fr.esrf.TangoApi.DeviceInfo;
import hzg.wpn.tango.client.proxy.TangoProxy;
import org.apache.commons.beanutils.ConvertUtils;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import java.util.Collection;

@Path("/")
public class TangoGateway {

    @GET
    @Path("devices")
    @Produces("application/json")
    public Collection<String> getDevices(@Context ServletContext ctx) throws Exception {//TODO handle exception
        DatabaseDs db = (DatabaseDs) ctx.getAttribute(DatabaseDs.TANGO_DB);
        return db.getDeviceList();
    }

    @GET
    @Path("device/{domain}/{name}/{instance}")
    //TODO marshaller
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
    public String getDeviceAttributes(@PathParam("domain") String domain,
                                      @PathParam("name") String name,
                                      @PathParam("instance") String instance,
                                      @Context ServletContext ctx) {
        return "List of device attributes";
    }

    @GET
    @Path("device/{domain}/{name}/{instance}/commands")
    public String getDeviceCommands(@PathParam("domain") String domain,
                                    @PathParam("name") String name,
                                    @PathParam("instance") String instance,
                                    @Context ServletContext ctx) {
        return "List of device commands";
    }

    @GET
    @Path("device/{domain}/{name}/{instance}/attribute/{attr}")
    public Object getAttribute(@PathParam("domain") String domain,
                               @PathParam("name") String name,
                               @PathParam("instance") String instance,
                               @PathParam("attr") String attr,
                               @Context ServletContext ctx) throws Exception {
        DeviceMapper mapper = (DeviceMapper) ctx.getAttribute(DeviceMapper.TANGO_MAPPER);
        TangoProxy device = mapper.map(domain + "/" + name + "/" + instance);
        return device.readAttributeValueTimeQuality(attr);
    }

    @PUT
    @Path("device/{domain}/{name}/{instance}/attribute/{attr}/argin={arg}")
    public void putAttribute(@PathParam("domain") String domain,
                             @PathParam("name") String name,
                             @PathParam("instance") String instance,
                             @PathParam("attr") String attr,
                             @PathParam("arg") String arg,
                             @Context ServletContext ctx) throws Exception {
        //TODO exceptions
        DeviceMapper mapper = (DeviceMapper) ctx.getAttribute(DeviceMapper.TANGO_MAPPER);
        TangoProxy device = mapper.map(domain + "/" + name + "/" + instance);
        Class<?> targetType = device.getAttributeInfo(attr).getClazz();
        Object converted = ConvertUtils.convert(arg, targetType);
        device.writeAttribute(attr, converted);
    }

    @PUT
    @Path("device/{domain}/{name}/{instance}/command/{cmd}/argin={arg}")
    public Object putCommand(@PathParam("domain") String domain,
                             @PathParam("name") String name,
                             @PathParam("instance") String instance,
                             @PathParam("cmd") String cmd,
                             @PathParam("arg") String arg,
                             @Context ServletContext ctx) throws Exception {
        //TODO exceptions
        DeviceMapper mapper = (DeviceMapper) ctx.getAttribute(DeviceMapper.TANGO_MAPPER);
        TangoProxy device = mapper.map(domain + "/" + name + "/" + instance);
        Class<?> targetType = device.getCommandInfo(cmd).getArginType();
        Object converted = ConvertUtils.convert(arg, targetType);
        return device.executeCommand(cmd, converted);
    }
}
