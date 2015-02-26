package org.tango.web.server.rest;

/**
 * @author Ingvord
 * @since 21.06.14
 */

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import fr.esrf.Tango.AttrDataFormat;
import fr.esrf.TangoApi.AttributeInfoEx;
import fr.esrf.TangoApi.CommandInfo;
import fr.esrf.TangoApi.DeviceInfo;
import org.apache.commons.beanutils.ConvertUtils;
import org.javatuples.Triplet;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.jboss.resteasy.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tango.client.ez.attribute.Quality;
import org.tango.client.ez.data.format.TangoDataFormat;
import org.tango.client.ez.proxy.*;
import org.tango.web.rest.DeviceState;
import org.tango.web.rest.Response;
import org.tango.web.rest.Responses;
import org.tango.web.server.DatabaseDs;
import org.tango.web.server.DeviceMapper;

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import java.awt.image.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ConcurrentMap;

@Path("/")
@NoCache
public class Rest2Tango {
    @GET
    @Path("devices")
    @Produces("application/json")
    public Response getDevices(@Context ServletContext ctx) throws Exception {//TODO handle exception
        DatabaseDs db = (DatabaseDs) ctx.getAttribute(DatabaseDs.TANGO_DB);
        return Responses.createSuccessResult(db.getDeviceList());
    }

    @GET
    @Path("device/{domain}/{name}/{instance}")
    @Produces("application/json")
    public DeviceState getDevice(@PathParam("domain") String domain,
                                 @PathParam("name") String name,
                                 @PathParam("instance") String instance,
                                 @Context ServletContext ctx) throws Exception {//TODO exceptions
        TangoProxy proxy = lookupTangoProxy(domain, name, instance, ctx);
        DeviceState result = new DeviceState(proxy.toDeviceProxy().state().toString(), proxy.toDeviceProxy().status());
        return result;
    }

    @GET
    @Path("device/{domain}/{name}/{instance}/info")
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
    public Response getDeviceAttributes(@PathParam("domain") String domain,
                                        @PathParam("name") String name,
                                        @PathParam("instance") String instance,
                                        @Context ServletContext ctx) throws Exception {
        TangoProxy proxy = lookupTangoProxy(domain, name, instance, ctx);
        Collection<String> result = Collections2.transform(Arrays.asList(proxy.toDeviceProxy().get_attribute_info_ex()), new Function<AttributeInfoEx, String>() {
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
                                      @Context ServletContext ctx) throws Exception {
        TangoProxy proxy = lookupTangoProxy(domain, name, instance, ctx);
        Collection<String> result = Collections2.transform(Arrays.asList(proxy.toDeviceProxy().command_list_query()), new Function<CommandInfo, String>() {
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
                                 @Context ServletContext ctx) throws Exception {
        TangoProxy proxy = lookupTangoProxy(domain, name, instance, ctx);
        if (proxy.hasCommand(member)) {
            Class<?> targetType = proxy.getCommandInfo(member).getArginType();
            if (targetType == Void.class) return Responses.createSuccessResult(proxy.executeCommand(member, null));
            Object converted = ConvertUtils.convert(arg, targetType);
            return Responses.createSuccessResult(proxy.executeCommand(member, converted));
        } else if (proxy.hasAttribute(member)) {
            return new WriteAttributeHelper(proxy).write(member, arg);
        } else
            throw new IllegalArgumentException(String.format("Device %s does not have neither attribute nor command %s", proxy.getName(), member));
    }

    @GET
    @Path("device/{domain}/{name}/{instance}/{cmd_or_attr}/info")
    @Produces("application/json")
    public Object getCommandOrAttributeInfo(@PathParam("domain") String domain,
                                            @PathParam("name") String name,
                                            @PathParam("instance") String instance,
                                            @PathParam("cmd_or_attr") String member,
                                            @Context ServletContext ctx) throws Exception {
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
                                        @Context ServletContext ctx) throws Exception {

        TangoProxy proxy = lookupTangoProxy(domain, name, instance, ctx);
        if (proxy.hasAttribute(member)) //TODO if attr image - generate one and send link
        {
            if(proxy.getAttributeInfo(member).getFormat().toAttrDataFormat() == AttrDataFormat.IMAGE){
                return new ImageAttributeHelper(member, proxy, ctx.getRealPath("/temp")).send();
            } else {
                Triplet<Object, Long, Quality> result = proxy.readAttributeValueTimeQuality(member);
                return Responses.createAttributeSuccessResult(new Triplet<>(result.getValue0(), result.getValue1(), result.getValue2().name()));
            }
        } else if (proxy.hasCommand(member))
            return Responses.createSuccessResult(proxy.executeCommand(member, null));
        else
            throw new IllegalArgumentException();
    }

    public static final long CAPACITY = 1000L;//TODO parameter
    private static final ConcurrentMap<String, EventHelper> event_helpers = new ConcurrentLinkedHashMap.Builder<String, EventHelper>()
            .maximumWeightedCapacity(CAPACITY)
            .build();

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
                                                @Context ServletContext ctx) throws Exception {

        TangoProxy proxy = lookupTangoProxy(domain, name, instance, ctx);
        String device_member = proxy.getName() + '/' + member;
        if (!proxy.hasAttribute(member))
            return Responses.createFailureResult(new String[]{"No such attr: " + device_member});

        TangoEvent event;
        try {
            event = TangoEvent.valueOf(evt.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Responses.createFailureResult(new String[]{"Unknown event type: " + evt});
        }
        try {
            device_member += '.' + evt;
            if (state == EventHelper.State.INITIAL) {
                EventHelper helper;
                helper = new EventHelper(member, event, proxy);
                EventHelper oldHelper = event_helpers.putIfAbsent(device_member, helper);
                if (oldHelper == null) {
                    //read initial value from the proxy
                    Triplet<?,Long, Quality> attrTimeQuality = proxy.readAttributeValueTimeQuality(member);
                    Response<?> result = Responses.createAttributeSuccessResult(
                            new Triplet<>(
                                    attrTimeQuality.getValue0(),
                                    attrTimeQuality.getValue1(),
                                    attrTimeQuality.getValue2().name()
                            ));

                    helper.set(result);
                    helper.subscribe();
                    return result;
                } else {
                    //block this servlet until event or timeout if it has no value
                    return oldHelper.hasValue() ? oldHelper.get() : oldHelper.get(timeout);
                }
            } else if(state == EventHelper.State.CONTINUATION){
                //block this servlet until event or timeout
                //TODO could throw NPE if cached value is garbage collected
                return event_helpers.get(device_member).get(timeout);
            } else {
            EventHelper helper = new EventHelper(member,event,proxy);
            helper.subscribe();
            //block this servlet until event or timeout
            return helper.get(timeout);
            }
        } catch (Exception e) {
            return Responses.createFailureResult(new String[]{e.getMessage()});
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
                             @Context ServletContext ctx) throws Exception {//TODO exceptions
        TangoProxy proxy = lookupTangoProxy(domain, name, instance, ctx);
        if (!proxy.hasCommand(cmd)) {
            //workaround jsonp limitation
            if (method != null && "PUT".equalsIgnoreCase(method) && proxy.hasAttribute(cmd)) {
                return new WriteAttributeHelper(proxy).write(cmd,arg);

            } else
                throw new IllegalArgumentException(String.format("Device %s does not have command %s", proxy.getName(), cmd));
        } else {
            Class<?> targetType = proxy.getCommandInfo(cmd).getArginType();
            if (targetType == Void.class) return proxy.executeCommand(cmd, null);
            Object converted = ConvertUtils.convert(arg, targetType);
            return Responses.createSuccessResult(proxy.executeCommand(cmd, converted));
        }
    }

    private TangoProxy lookupTangoProxy(String domain, String name, String instance, ServletContext ctx) throws TangoProxyException {
        DeviceMapper mapper = (DeviceMapper) ctx.getAttribute(DeviceMapper.TANGO_MAPPER);
        return mapper.map(domain + "/" + name + "/" + instance);
    }

    public static class ImageAttributeHelper {
        private String attribute;
        private TangoProxy proxy;
        private java.nio.file.Path root;

        public ImageAttributeHelper(String attr_name, TangoProxy proxy, String root) {
            this.attribute = attr_name;
            this.proxy = proxy;
            this.root = Paths.get(root);
        }


        public Response<String> send() throws TangoProxyException, IOException{
            //the first is a two dim array
            Triplet<Object,Long,Quality> valueTimeQuality = proxy.readAttributeValueTimeQuality(attribute);
            BufferedImage image = attributeToImage(valueTimeQuality.getValue0());
            String device_dir_name = proxy.getName().replaceAll("\\/", "_");
            java.nio.file.Path device_dir = root.resolve(device_dir_name);
            if(!Files.exists(device_dir)){
                device_dir = Files.createDirectories(device_dir);
            }

            java.nio.file.Path tmp_image_file = device_dir.resolve(attribute + ".jpeg");
            String tmp_image_file_path = tmp_image_file.subpath(tmp_image_file.getNameCount() - 3,tmp_image_file.getNameCount()).toString().replace('\\', '/');
            //TODO if debug perform asynch write into the FileSystem
            ByteArrayOutputStream bos = new ByteArrayOutputStream(image.getHeight() * image.getWidth() * 4);
            if(ImageIO.write(image, "jpeg", bos))
                return Responses.createSuccessResult("data:image/jpeg;base64,"+ Base64.encodeBytes(bos.toByteArray()));
            else
                return Responses.createFailureResult(new String[]{"Cannot save image!"});
        }

        private int resolveImageType(Class<?> dataType){
            if(dataType == double.class || dataType == float.class)
                throw new UnsupportedOperationException("Reading of floating based images is not yet implemented");
            else
                return BufferedImage.TYPE_USHORT_555_RGB;
        }

        private BufferedImage attributeToImage(Object data){
            int height = Array.getLength(data);
            int width = Array.getLength(Array.get(data,0));

            BufferedImage imgResult = new BufferedImage(width, height, resolveImageType(data.getClass().getComponentType().getComponentType()));
            for (int i = 0, x = 0, y = 0, size = width * height;
                 i < size;
                 i++, x = x < (width - 1) ? x + 1 : 0, y += x == 0 ? 1 : 0) {
                imgResult.setRGB(x, y, Array.getInt(Array.get(data, y), x));
            }
            return imgResult;
        }
    }
    /**
     * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
     * @since 20.02.2015
     */
    @ThreadSafe
    public static class EventHelper {
        //TODO parameters
        private static final long MAX_AWAIT = 30000L;
        private static final long DELTA = 256L;

        private static final Logger log = LoggerFactory.getLogger(EventHelper.class);

        public static enum State {
            UNDEFINED,
            INITIAL,
            CONTINUATION
        }

        private final String attribute;
        private final TangoEvent evt;
        private final TangoProxy proxy;

        private volatile Response<?> value;

        private final Object guard = new Object();
        public EventHelper(String attribute, TangoEvent evt, TangoProxy proxy) {
            this.attribute = attribute;
            this.evt = evt;
            this.proxy = proxy;
        }

        public void set(Response<?> value) {
            synchronized (guard){
                this.value = value;
                guard.notifyAll();
            }
        }

        public boolean hasValue(){
            return value != null;
        }

        public Response<?> get(){
            return value;
        }

        /**
         * Waits for value to be set
         *
         * If value is not set before timeout then returns stored value
         *
         * @param timeout
         * @return
         * @throws InterruptedException
         */
        public Response<?> get(long timeout) throws InterruptedException{
            synchronized (guard){
                do
                    guard.wait((timeout = timeout - DELTA) > 0 ? timeout : MAX_AWAIT);
                while (value == null);
            }
            return value;
        }

        private TangoEventListener<Object> listener;
        public void subscribe() throws TangoProxyException{
            proxy.subscribeToEvent(attribute, evt);

            listener = new TangoEventListener<Object>() {
                @Override
                public void onEvent(EventData<Object> data) {
                    log.debug(proxy.getName() +"/" + attribute + "." + evt +" onEvent!");
                    EventHelper.this.set(Responses.createAttributeSuccessResult(new Triplet<>(data.getValue(), data.getTime(), Quality.VALID.name())));
                }

                @Override
                public void onError(Throwable cause) {
                    log.debug(proxy.getName() +"/" + attribute + "." + evt +" onError!");
                    EventHelper.this.set(Responses.createFailureResult(new String[]{cause.getMessage()}));
                }
            };
            proxy.addEventListener(attribute, evt, listener);
        }
    }

    private static class WriteAttributeHelper {
        private TangoProxy proxy;

        public WriteAttributeHelper(TangoProxy proxy) {
            this.proxy = proxy;
        }

        public Response<Void> write(String attrName, String arg) throws TangoProxyException {
            TangoAttributeInfoWrapper attributeInfo = proxy.getAttributeInfo(attrName);
            Class<?> targetType = attributeInfo.getClazz();
            Object converted = ConvertUtils.convert(arg, targetType);
            proxy.writeAttribute(attrName, converted);
            //TODO read actual value
            return Responses.createSuccessResult(null);
        }
    }
}
