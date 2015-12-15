package org.tango.web.server.tango;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevSource;
import fr.esrf.Tango.DevState;
import fr.esrf.Tango.DevVarLongStringArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tango.DeviceState;
import org.tango.client.database.DatabaseFactory;
import org.tango.client.ez.proxy.TangoProxy;
import org.tango.client.ez.util.TangoUtils;
import org.tango.server.ServerManager;
import org.tango.server.annotation.*;
import org.tango.server.export.IExporter;
import org.tango.server.servant.DeviceImpl;
import org.tango.web.server.TangoContext;
import org.tango.web.server.TangoProxyCreationPolicy;

import javax.servlet.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 14.12.2015
 */

public class TangoRestApiDeviceStarter implements ServletContextListener {
    private static final Logger logger = LoggerFactory.getLogger(TangoRestApiDeviceStarter.class);

    public static final String START_TANGO = "org.tango.rest.server.tango.start";
    public static final String TANGO_INSTANCE = "org.tango.rest.server.tango.instance";

    /**
     * Called by servlet container
     *
     * @param sce
     */
    public void contextInitialized(ServletContextEvent sce) {
        if (!Boolean.parseBoolean(sce.getServletContext().getInitParameter(START_TANGO))) return;
        logger.info("Starting Tango REST device server...");
        String instance = sce.getServletContext().getInitParameter(TANGO_INSTANCE);
        ServerManager.getInstance().start(new String[]{instance}, TangoRestServer.class);

        try {
            Field tangoExporterField = ServerManager.getInstance().getClass().getDeclaredField("tangoExporter");
            tangoExporterField.setAccessible(true);
            IExporter tangoExporter = (IExporter) tangoExporterField.get(ServerManager.getInstance());


            final String[] deviceList = DatabaseFactory.getDatabase().getDeviceList(
                    TangoRestServer.class.getSimpleName() + "/" + instance, TangoRestServer.class.getSimpleName());

            if (deviceList.length == 0)
                throw new RuntimeException("Can not start TangoRestServer - no devices was found");

            DeviceImpl device = tangoExporter.getDevice(TangoRestServer.class.getSimpleName(), deviceList[0]);
            ((TangoRestServer) device.getBusinessObject()).ctx =
                    (TangoContext) sce.getServletContext().getAttribute(TangoContext.TANGO_CONTEXT);

            logger.info("Done.");
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (DevFailed devFailed) {
            throw new RuntimeException(TangoUtils.convertDevFailedToException(devFailed));
        }
    }


    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        try {
            if(ServerManager.getInstance().isStarted()) {
                logger.info("Stopping Tango REST device server...");
                ServerManager.getInstance().stop();
            }
            logger.info("Done.");
        } catch (DevFailed devFailed) {
            throw new RuntimeException(TangoUtils.convertDevFailedToException(devFailed));
        }
    }


    @Device
    public static class TangoRestServer {
        private volatile TangoContext ctx;

        @State
        private DevState state = DevState.OFF;

        public void setState(DevState state) {
            this.state = state;
        }

        public DevState getState() {
            return state;
        }

        @Init
        @StateMachine(endState = DeviceState.ON)
        public void initTango() {

        }

        @Attribute
        public String[] getAliveProxies() throws Exception {
            List<String> result = Lists.transform(
                    ctx.deviceMapper.proxies(),
                    new Function<TangoProxy, String>() {
                        @Override
                        public String apply(TangoProxy input) {
                            return input.getName();
                        }
                    });

            return result.toArray(new String[result.size()]);
        }

        @Command(inTypeDesc = "deviceName->value")
        public void setProxiesSource(DevVarLongStringArray input) throws Exception {
            String[] svalue = input.svalue;
            for (int i = 0, svalueLength = svalue.length; i < svalueLength; i++) {
                String device = svalue[i];
                TangoProxy proxy = ctx.deviceMapper.map(device);
                DevSource new_src = DevSource.from_int(input.lvalue[i]);
                proxy.toDeviceProxy().set_source(new_src);
                ctx.tangoProxyCreationPolicies.put(proxy.getName(), new TangoProxyCreationPolicy(new_src));
            }
        }

        @Attribute
        @AttributeProperties(unit = "millis")
        public long getProxyKeepAliveDelay(){
            return TimeUnit.MILLISECONDS.convert(ctx.tangoProxyKeepAliveDelay, ctx.tangoProxyKeepAliveDelayTimeUnit);
        }

        @Attribute
        @AttributeProperties(unit = "millis")
        public void setProxyKeepAliveDelay(long millis){
            ctx.tangoProxyKeepAliveDelay = ctx.tangoProxyKeepAliveDelayTimeUnit.convert(millis, TimeUnit.MILLISECONDS);
        }

        @Delete
        @StateMachine(endState = DeviceState.OFF)
        public void delete() {
        }
    }


}
