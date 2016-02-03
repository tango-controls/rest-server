package org.tango.rest.mtango;

/**
 * @author Ingvord
 * @since 21.06.14
 */

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import fr.esrf.Tango.AttrDataFormat;
import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.esrf.TangoApi.AttributeInfoEx;
import fr.esrf.TangoApi.CommandInfo;
import fr.esrf.TangoApi.DeviceInfo;
import fr.esrf.TangoDs.TangoConst;
import org.apache.commons.beanutils.ConvertUtils;
import org.javatuples.Triplet;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.jboss.resteasy.util.Base64;
import org.tango.client.ez.attribute.Quality;
import org.tango.client.ez.data.type.TangoImage;
import org.tango.client.ez.proxy.*;
import org.tango.client.ez.util.TangoImageUtils;
import org.tango.client.ez.util.TangoUtils;
import org.tango.rest.entities.DeviceState;
import org.tango.rest.response.Response;
import org.tango.web.server.DatabaseDs;
import org.tango.web.server.DeviceMapper;
import org.tango.web.server.EventHelper;
import org.tango.rest.response.Responses;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import java.awt.image.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

@Path("/mtango")
@NoCache
public class MtangoImpl {
    @GET
    @Path("devices")
    @Produces("application/json")
    public Response getDevices(@Context ServletContext ctx) {
        DatabaseDs db = (DatabaseDs) ctx.getAttribute(DatabaseDs.TANGO_DB);
        try {
            Collection<String> deviceList = db.getDeviceList();
            return Responses.createSuccessResult(deviceList);
        } catch (NoSuchCommandException|TangoProxyException e) {
            return Responses.createFailureResult("Can not get device list from the db " + db.getDbURL(),e);
        }
    }

    @GET
    @Path("domains")
    @Produces("application/json")
    public Response getDomains(@Context ServletContext ctx) {
        DatabaseDs db = (DatabaseDs) ctx.getAttribute(DatabaseDs.TANGO_DB);
        try {
            Collection<String> deviceList = db.getDomainsList();
            return Responses.createSuccessResult(deviceList);
        } catch (NoSuchCommandException|TangoProxyException e) {
            return Responses.createFailureResult("Can not get device list from the db " + db.getDbURL(),e);
        }
    }

    @GET
    @Path("device")
    @Produces("application/json")
    public Response getDomains0(@Context ServletContext ctx) {
        DatabaseDs db = (DatabaseDs) ctx.getAttribute(DatabaseDs.TANGO_DB);
        try {
            Collection<String> deviceList = db.getDomainsList();
            return Responses.createSuccessResult(deviceList);
        } catch (NoSuchCommandException|TangoProxyException e) {
            return Responses.createFailureResult("Can not get device list from the db " + db.getDbURL(),e);
        }
    }

    @GET
    @Path("families")
    @Produces("application/json")
    public Response getFamilies(@PathParam("domain") String ppDomain,
                               @QueryParam("domain") String qpDomain,
                               @Context ServletContext ctx) {
        DatabaseDs db = (DatabaseDs) ctx.getAttribute(DatabaseDs.TANGO_DB);
        String domain = "*";
        try {
            if(ppDomain != null) domain = ppDomain;
            if(qpDomain != null) domain = qpDomain;
            Collection<String> deviceList = db.getFamiliesList(domain);
            return Responses.createSuccessResult(deviceList);
        } catch (NoSuchCommandException|TangoProxyException e) {
            return Responses.createFailureResult("Can not get device list from the db " + db.getDbURL(),e);
        }
    }

    @GET
    @Path("device/{domain}")
    @Produces("application/json")
    public Response getFamilies(@PathParam("domain") String domain,
                                @Context ServletContext ctx) {
        DatabaseDs db = (DatabaseDs) ctx.getAttribute(DatabaseDs.TANGO_DB);
        try {
            Collection<String> deviceList = db.getFamiliesList(domain);
            return Responses.createSuccessResult(deviceList);
        } catch (NoSuchCommandException|TangoProxyException e) {
            return Responses.createFailureResult("Can not get device list from the db " + db.getDbURL(),e);
        }
    }


    @GET
    @Path("members")
    @Produces("application/json")
    public Response getDomains(@QueryParam("domain") String domain,
                               @QueryParam("family") String family,
                               @Context ServletContext ctx) {
        DatabaseDs db = (DatabaseDs) ctx.getAttribute(DatabaseDs.TANGO_DB);
        try {
            if(domain == null) domain = "*";
            if(family == null) family = "*";
            Collection<String> deviceList = db.getMembersList(domain, family);
            return Responses.createSuccessResult(deviceList);
        } catch (NoSuchCommandException|TangoProxyException e) {
            return Responses.createFailureResult("Can not get device list from the db " + db.getDbURL(),e);
        }
    }

    @GET
    @Path("device/{domain}/{family}")
    @Produces("application/json")
    public Response getMembers(@PathParam("domain") String domain,
                               @PathParam("family") String family,
                               @Context ServletContext ctx) {
        DatabaseDs db = (DatabaseDs) ctx.getAttribute(DatabaseDs.TANGO_DB);
        try {
            Collection<String> deviceList = db.getMembersList(domain, family);
            return Responses.createSuccessResult(deviceList);
        } catch (NoSuchCommandException|TangoProxyException e) {
            return Responses.createFailureResult("Can not get device list from the db " + db.getDbURL(),e);
        }
    }

    @GET
    @Path("device/{domain}/{name}/{instance}")
    @Produces("application/json")
    public DeviceState getDevice(@PathParam("domain") String domain,
                                 @PathParam("name") String name,
                                 @PathParam("instance") String instance,
                                 @Context ServletContext ctx) throws DevFailed {
        TangoProxy proxy = null;
        try {
            proxy = lookupTangoProxy(domain, name, instance, ctx);
        } catch (TangoProxyException e) {
            return new DeviceState(
                    DevState.UNKNOWN.toString(),String.format("Can not get proxy for device[%s/%s/%s]",domain,name,instance));
        }
        DeviceState result = new DeviceState(proxy.toDeviceProxy().state().toString(), proxy.toDeviceProxy().status());
        return result;
    }

    @GET
    @Path("device/{domain}/{name}/{instance}/info")
    @Produces("application/json")
    public DeviceInfo getDeviceInfo(@PathParam("domain") String domain,
                                    @PathParam("name") String name,
                                    @PathParam("instance") String instance,
                                    @Context ServletContext ctx) throws TangoProxyException, NoSuchCommandException {
        DatabaseDs db = (DatabaseDs) ctx.getAttribute(DatabaseDs.TANGO_DB);
        DeviceInfo info = db.getDeviceInfo(domain + "/" + name + "/" + instance);
        return info;
    }

    @GET
    @Path("device/{domain}/{name}/{instance}/attributes")
    @Produces("application/json")
    public Response getDeviceAttributes(@PathParam("domain") String domain,
                                        @PathParam("name") String name,
                                        @PathParam("instance") String instance,
                                        @Context ServletContext ctx) {
        TangoProxy proxy = null;
        try {
            proxy = lookupTangoProxy(domain, name, instance, ctx);
        } catch (TangoProxyException e) {
            return Responses.createFailureResult(String.format("Can not find proxy for device[%s/%s/%s]",domain,name,instance),e);
        }
        List<AttributeInfoEx> attributeInfoExes = null;
        try {
            attributeInfoExes = Arrays.asList(proxy.toDeviceProxy().get_attribute_info_ex());
        } catch (DevFailed devFailed) {
            return Responses.createFailureResult(
                    String.format("Can not get attribute info list from the device[%s/%s/%s]",domain,name,instance),
                    TangoUtils.convertDevFailedToException(devFailed));
        }

        Collection<String> result = Collections2.transform(attributeInfoExes, new Function<AttributeInfoEx, String>() {
            @Override
            public String apply(@Nullable AttributeInfoEx input) {
                return input.name;
            }
        });
        return Responses.createSuccessResult(result);
    }

    @GET
    @Path("device/{domain}/{name}/{instance}/commands")
    @Produces("application/json")
    public Response getDeviceCommands(@PathParam("domain") String domain,
                                      @PathParam("name") String name,
                                      @PathParam("instance") String instance,
                                      @Context ServletContext ctx) {
        TangoProxy proxy = null;
        try {
            proxy = lookupTangoProxy(domain, name, instance, ctx);
        } catch (TangoProxyException e) {
            return Responses.createFailureResult(String.format("Can not find proxy for device[%s/%s/%s]",domain,name,instance),e);
        }

        List<CommandInfo> commandInfos = null;
        try {
            commandInfos = Arrays.asList(proxy.toDeviceProxy().command_list_query());
        } catch (DevFailed devFailed) {
            return Responses.createFailureResult(
                    String.format("Can not get commands list from the device[%s/%s/%s]",domain,name,instance),
                    TangoUtils.convertDevFailedToException(devFailed));
        }

        Collection<String> result = Collections2.transform(commandInfos, new Function<CommandInfo, String>() {
            @Override
            public String apply(@Nullable CommandInfo input) {
                return input.cmd_name;
            }
        });
        return Responses.createSuccessResult(result);
    }

    @PUT
    @Path("device/{domain}/{name}/{instance}/{member}={arg}")
    @Produces("application/json")
    public Response putAttribute(@PathParam("domain") String domain,
                                 @PathParam("name") String name,
                                 @PathParam("instance") String instance,
                                 @PathParam("member") String member,
                                 @PathParam("arg") String arg,
                                 @Context ServletContext ctx) throws TangoProxyException, NoSuchAttributeException {
        TangoProxy proxy = null;
        try {
            proxy = lookupTangoProxy(domain, name, instance, ctx);
        } catch (TangoProxyException e) {
            return Responses.createFailureResult(
                    String.format("Can not find proxy for device[%s/%s/%s]",domain,name,instance),e);
        }

        if (proxy.hasCommand(member)) {
            return new CommandHelper(proxy).execute(member, arg);
        } else if (proxy.hasAttribute(member)) {
            return new WriteAttributeHelper(proxy).write(member, arg);
        } else
            return Responses.createFailureResult(
                    String.format("Device[%s] does not have neither attribute nor command[%s]", proxy.getName(), member));
    }

    @GET
    @Path("device/{domain}/{name}/{instance}/{cmd_or_attr}/info")
    @Produces("application/json")
    public Object getCommandOrAttributeInfo(@PathParam("domain") String domain,
                                            @PathParam("name") String name,
                                            @PathParam("instance") String instance,
                                            @PathParam("cmd_or_attr") String member,
                                            @Context ServletContext ctx) throws TangoProxyException, NoSuchAttributeException, NoSuchCommandException {
        TangoProxy proxy = lookupTangoProxy(domain, name, instance, ctx);
        if (proxy.hasAttribute(member))
            return proxy.getAttributeInfo(member).toAttributeInfo();
        else
            return proxy.getCommandInfo(member).toCommandInfo();
    }

    @GET
    @Path("device/{domain}/{name}/{instance}/{cmd_or_attr}")
    @Produces("application/json")
    public Object getCommandOrAttribute(@PathParam("domain") String domain,
                                        @PathParam("name") String name,
                                        @PathParam("instance") String instance,
                                        @PathParam("cmd_or_attr") String member,
                                        @Context ServletContext ctx,
                                        @Context HttpServletResponse response) throws TangoProxyException, IOException, NoSuchCommandException, NoSuchAttributeException {

        TangoProxy proxy = null;
        try {
            proxy = lookupTangoProxy(domain, name, instance, ctx);
        } catch (TangoProxyException e) {
            return Responses.createFailureResult(
                    String.format("Can not find proxy for device[%s/%s/%s]",domain,name,instance),e);
        }

        if (proxy.hasAttribute(member))
        {
            if(proxy.getAttributeInfo(member).getFormat().toAttrDataFormat() == AttrDataFormat.IMAGE){
                new ImageAttributeHelper(proxy, ctx.getRealPath("/temp")).send(member,response.getOutputStream());
                return null;
            } if(proxy .getAttributeInfo(member).toAttributeInfo().data_type == TangoConst.Tango_DEV_ENCODED) {
                new EncodedAttributeHelper(proxy, ctx.getRealPath("/temp")).send(member,response.getOutputStream());
                return null;
            } else {
                return new ReadAttributeHelper(proxy).read(member);
            }
        } else if (proxy.hasCommand(member))
            return Responses.createSuccessResult(proxy.executeCommand(member, null));
        else
            return Responses.createFailureResult(String.format("Device[%s] does not have neither attribute nor command[%s]", proxy.getName(), member));
    }

    @GET
    @Path("device/{domain}/{name}/{instance}/{attr}.{evt}")
    @Produces("application/json")
    public Object getCommandOrAttributeOnChange(@PathParam("domain") String domain,
                                                @PathParam("name") String name,
                                                @PathParam("instance") String instance,
                                                @PathParam("attr") String member,
                                                @PathParam("evt") String evt,
                                                @QueryParam("timeout") long timeout,
                                                @QueryParam("state") EventHelper.State state,
                                                @Context ServletContext ctx) throws TangoProxyException, InterruptedException {

        TangoProxy proxy = null;
        try {
            proxy = lookupTangoProxy(domain, name, instance, ctx);
        } catch (TangoProxyException e) {
            return Responses.createFailureResult(
                    String.format("Can not find proxy for device[%s/%s/%s]",domain,name,instance),e);
        }

        String device_member = proxy.getName() + '/' + member;
        if (!proxy.hasAttribute(member))
            return Responses.createFailureResult("No such attr: " + device_member);

        TangoEvent event;
        try {
            event = TangoEvent.valueOf(evt.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Responses.createFailureResult("Unknown event type: " + evt);
        }
        device_member += '.' + evt;
        try {
            return EventHelper.handleEvent(member, timeout, state, proxy, event);
        } catch (NoSuchAttributeException|TangoProxyException e) {
            return Responses.createFailureResult("Failed to subscribe to event " + device_member,e);
        }
    }

    @GET
    @Path("device/{domain}/{name}/{instance}/{cmd_or_attr}={arg}")//TODO optional argument
    @Produces("application/json")
    public Object getCommand(@PathParam("domain") String domain,
                             @PathParam("name") String name,
                             @PathParam("instance") String instance,
                             @PathParam("cmd_or_attr") String cmd,
                             @PathParam("arg") String arg,
                             @QueryParam("_method") String method,
                             @Context ServletContext ctx) throws TangoProxyException, NoSuchAttributeException {
        TangoProxy proxy = null;
        try {
            proxy = lookupTangoProxy(domain, name, instance, ctx);
        } catch (TangoProxyException e) {
            return Responses.createFailureResult(
                    String.format("Can not find proxy for device[%s/%s/%s]",domain,name,instance),e);
        }
        if (!proxy.hasCommand(cmd)) {
            //workaround jsonp limitation
            if (method != null && "PUT".equalsIgnoreCase(method) && proxy.hasAttribute(cmd)) {
                return new WriteAttributeHelper(proxy).write(cmd, arg);
            } else
                return Responses.createFailureResult(String.format("Device %s does not have command %s", proxy.getName(), cmd));
        } else {
            return new CommandHelper(proxy).execute(cmd, arg);
        }
    }

    private static TangoProxy lookupTangoProxy(String domain, String name, String instance, ServletContext ctx) throws TangoProxyException {
        DeviceMapper mapper = (DeviceMapper) ctx.getAttribute(DeviceMapper.TANGO_MAPPER);
        return mapper.map(domain + "/" + name + "/" + instance);
    }

    public static class ImageAttributeHelper {
        private TangoProxy proxy;
        private java.nio.file.Path root;

        public ImageAttributeHelper(TangoProxy proxy, String root) {
            this.proxy = proxy;
            this.root = Paths.get(root);
        }

        public void send(String attribute, OutputStream responseStream) throws IOException {
            Writer writer = new BufferedWriter(new OutputStreamWriter(responseStream));
            ValueTimeQuality<?> valueTimeQuality = null;
            try {
                valueTimeQuality = proxy.readAttributeValueTimeQuality(attribute);
            } catch (NoSuchAttributeException|TangoProxyException e) {
                Responses.sendFailure(
                        new Exception(String.format("Failed to read image[%s/%s]",proxy.getName(),attribute),e), writer);
                writer.close();
                return;
            }
            RenderedImage image = getImage(valueTimeQuality,writer);
            if(image == null) return;
            //TODO if debug perform asynch write into the FileSystem

            OutputStream out = new Base64.OutputStream(responseStream);

            writer.write("{\"argout\":\"data:/jpeg;base64,");
            writer.flush();
            if(ImageIO.write(image, "jpeg", out)) {
                writer.write("\",\"quality\":\"VALID\"");
            } else {
                writer.write("\",\"errors\":[\"Failed to commit image into response!\"],\"quality\":\"INVALID\"");
            }
            writer.write(",\"timestamp\":");
            writer.write(Long.toString(valueTimeQuality.getTime()));
            writer.write("}");

            writer.flush();
            writer.close();
        }

        RenderedImage getImage(ValueTimeQuality<?> valueTimeQuality, Writer writer) throws IOException{
            //the first is a two dim array
            TangoImage<?> tangoImage = (TangoImage<?>) valueTimeQuality.getValue();

            Class<?> componentType = tangoImage.getData().getClass().getComponentType();
            if(componentType != int.class) {
                Responses.sendFailure(
                        new Exception("Unsupported image component type: " + componentType.getSimpleName()), writer);
                writer.close();
                return null;
            }
            return TangoImageUtils.toRenderedImage_sRGB((int[]) tangoImage.getData(), tangoImage.getWidth(), tangoImage.getHeight());
        }
    }

    public class EncodedAttributeHelper extends ImageAttributeHelper{
        public EncodedAttributeHelper(TangoProxy proxy, String realPath) {
            super(proxy, realPath);
        }

        @Override
        RenderedImage getImage(ValueTimeQuality<?> valueTimeQuality, Writer writer){
            return (RenderedImage) valueTimeQuality.getValue();
        }
    }

    private static class CommandHelper {
        private TangoProxy proxy;

        CommandHelper(TangoProxy proxy){
            this.proxy = proxy;
        }


        Response execute(String member, String arg) {
            Class<?> targetType = null;
            try {
                targetType = proxy.getCommandInfo(member).getArginType();
            } catch (NoSuchCommandException|TangoProxyException e) {
                return Responses.createFailureResult(
                        String.format("Can not get info for command[%s/%s]",proxy.getName(),member),e);
            }
            Object argin = null;
            if (targetType != Void.class)
                argin = ConvertUtils.convert(arg, targetType);

            try {
                return Responses.createSuccessResult(proxy.executeCommand(member, argin));
            } catch (NoSuchCommandException|TangoProxyException e){
                return Responses.createFailureResult(
                        String.format("Can not execute command[%s/%s]", proxy.getName(), member), e);
            }
        }
    }

    private static class ReadAttributeHelper {
        private TangoProxy proxy;

        ReadAttributeHelper(TangoProxy proxy) {
            this.proxy = proxy;
        }

        Response read(String attr){
            ValueTimeQuality<Object> result = null;
            try {
                result = proxy.readAttributeValueTimeQuality(attr);
                return Responses.createAttributeSuccessResult(
                        result.getValue(), result.getTime(), result.getQuality().name());
            } catch (NoSuchAttributeException|TangoProxyException e) {
                return Responses.createFailureResult(
                        String.format("Can not read attribute[%s/%s]",proxy.getName(),attr),e);
            }

        }
    }

    private static class WriteAttributeHelper {
        private TangoProxy proxy;

        WriteAttributeHelper(TangoProxy proxy) {
            this.proxy = proxy;
        }

        Response<Void> write(String attrName, String arg) throws TangoProxyException, NoSuchAttributeException {
            TangoAttributeInfoWrapper attributeInfo = proxy.getAttributeInfo(attrName);
            Class<?> targetType = attributeInfo.getClazz();
            Object converted = ConvertUtils.convert(arg, targetType);
            proxy.writeAttribute(attrName, converted);
            //TODO read actual value
            return Responses.createSuccessResult(null);
        }
    }
}
