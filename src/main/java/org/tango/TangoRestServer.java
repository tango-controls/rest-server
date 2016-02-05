package org.tango;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevSource;
import fr.esrf.Tango.DevState;
import fr.esrf.Tango.DevVarLongStringArray;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.loader.WebappLoader;
import org.apache.catalina.startup.Tomcat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tango.client.ez.proxy.TangoProxies;
import org.tango.client.ez.proxy.TangoProxy;
import org.tango.client.ez.proxy.TangoProxyException;
import org.tango.server.ServerManager;
import org.tango.server.ServerManagerUtils;
import org.tango.server.annotation.*;
import org.tango.web.server.*;

import javax.servlet.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 14.12.2015
 */
@Device
public class TangoRestServer {
    private static final Logger logger = LoggerFactory.getLogger(TangoRestServer.class);

    private static final Path tomcatBaseDir;

    public static final String WEBAPP_WAR = "webapp.war";

    static {

        try {
            tomcatBaseDir = Files.createTempDirectory("tomcat_");
            Files.createDirectory(tomcatBaseDir.resolve("webapps"));
        } catch (IOException e) {
            throw new RuntimeException("Can not create tomcat temp dir", e);
        }
        try {
            InputStream webapp = TangoRestServer.class.getResourceAsStream("/webapp.war");


            Files.copy(webapp, tomcatBaseDir.resolve(WEBAPP_WAR), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Can not extract webapp.war to a temp dir", e);
        }
    }

    public static final String TANGO_DB = "TANGO_DB";
    public static final String TANGO_ACCESS = "TANGO_ACCESS";
    public static final String TOMCAT_PORT = "TOMCAT_PORT";
    public static final String TOMCAT_AUTH_CONFIG = "TOMCAT_AUTH_CONFIG_CLASS";

    public static final String SYS_DATABASE_2 = "sys/database/2";
    public static final String SYS_ACCESS_CONTROL_1 = "sys/access_control/1";
    public static final String TANGO_INSTANCE = "tango.rest.server.instance";
    public static final String DEFAULT_AUTH_CLASS = "org.tango.web.server.PlainTextAuthConfiguration";


    @DeviceProperty(name = TANGO_DB, defaultValue = SYS_DATABASE_2)
    private String tangoDbProp;

    @DeviceProperty(name = TANGO_ACCESS, defaultValue = SYS_ACCESS_CONTROL_1)
    private String tangoAccessProp;

    @DeviceProperty(name = TOMCAT_PORT, defaultValue = "8844")
    private int tomcatPort = 8844;

    @DeviceProperty(name = TOMCAT_AUTH_CONFIG, defaultValue = DEFAULT_AUTH_CLASS)
    private String tomcatAuthConfigurationClass;

    @State
    private DevState state = DevState.OFF;

    private String tangoDbHost;


    public void setState(DevState state) {
        this.state = state;
    }

    public DevState getState() {
        return state;
    }

    private Tomcat tomcat;

    @Init
    @StateMachine(endState = DeviceState.RUNNING)
    public void init() throws DevFailed, ServletException, TangoProxyException, LifecycleException {
        logger.trace("Init'ing TangoRestServer device...");
        tangoDbProp = System.getProperty(TANGO_DB, tangoDbProp);
        logger.debug("TANGO_DB={}", tangoDbProp);
        System.setProperty(TANGO_DB, tangoDbProp);
        tangoAccessProp = System.getProperty(TANGO_ACCESS, tangoAccessProp);
        logger.debug("TANGO_ACCESS={}", tangoAccessProp);
        System.setProperty(TANGO_ACCESS, tangoAccessProp);


        TangoProxy dbProxy = TangoProxies.newDeviceProxyWrapper(tangoDbProp);
        DatabaseDs databaseDs = new DatabaseDs(dbProxy);

        tangoDbHost = dbProxy.toDeviceProxy().get_tango_host();
        logger.debug("TANGO_DB_HOST={}", tangoDbHost);
        if (tangoDbHost.endsWith("10000")) tangoDbHost = tangoDbHost.substring(0, tangoDbHost.indexOf(':'));

        tomcatPort = Integer.parseInt(System.getProperty(TOMCAT_PORT, Integer.toString(tomcatPort)));
    }

    @Delete
    @StateMachine(endState = DeviceState.OFF)
    public void delete() throws LifecycleException {
        if(tomcat != null) {
            tomcat.stop();
            tomcat.destroy();
        }
    }

    public static void main(String[] args) throws Exception {
        String instance = args[0];
        System.setProperty(TANGO_INSTANCE, instance);
        ServerManager.getInstance().start(args, TangoRestServer.class);

        startTomcat(instance);
    }

    private static void startTomcat(String instance) throws Exception {
        List<TangoRestServer> tangoRestServers = ServerManagerUtils.getBusinessObjects(instance, TangoRestServer.class);

        for (TangoRestServer tangoRestServer : tangoRestServers) {
            logger.trace("Configure tomcat for device");
            tangoRestServer.tomcat = new Tomcat();
            tangoRestServer.tomcat.setPort(tangoRestServer.tomcatPort);
            tangoRestServer.tomcat.setBaseDir(tomcatBaseDir.toAbsolutePath().toString());

            logger.trace("Add webapp[{}] tomcat for device", tangoRestServer.tangoDbHost);
            org.apache.catalina.Context context =
                    tangoRestServer.tomcat.addWebapp(tangoRestServer.tangoDbHost, tomcatBaseDir.resolve(WEBAPP_WAR).toAbsolutePath().toString());

            WebappLoader loader =
                    new WebappLoader(Thread.currentThread().getContextClassLoader());
            context.setLoader(loader);

            logger.trace("Configure tomcat auth for device");
            Object authConfig =
                    TangoRestServer.class.getClassLoader().loadClass(tangoRestServer.tomcatAuthConfigurationClass).newInstance();

            authConfig.getClass().getMethod("configure", Tomcat.class)
                    .invoke(authConfig, tangoRestServer.tomcat);

            logger.trace("Start tomcat of device");
            tangoRestServer.tomcat.start();
        }

        logger.trace("Done.");
    }

    public volatile TangoContext ctx;


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
    public boolean getCacheEnabled() {
        return ctx.isCacheEnabled;
    }

    @Attribute
    public void setCacheEnabled(boolean v) {
        ctx.isCacheEnabled = v;
    }

    @Attribute
    @AttributeProperties(unit = "millis")
    public void setProxyKeepAliveDelay(long millis) {
        ctx.tangoProxyKeepAliveDelay = ctx.tangoProxyKeepAliveDelayTimeUnit.convert(millis, TimeUnit.MILLISECONDS);
    }

    @Attribute
    @AttributeProperties(unit = "millis")
    public long getAttributeValueExpirationDelay() {
        return ctx.attributeValueExpirationDelay;
    }

    @Attribute
    @AttributeProperties(unit = "millis")
    public void setAttributeValueExpirationDelay(long v) {
        ctx.attributeValueExpirationDelay = v;
    }


    @Attribute
    @AttributeProperties(unit = "millis")
    public long getStaticValueExpirationDelay() {
        return ctx.staticDataExpirationDelay;
    }

    @Attribute
    @AttributeProperties(unit = "millis")
    public void setStaticValueExpirationDelay(long v) {
        ctx.staticDataExpirationDelay = v;
    }

    public void setTangoDbProp(String tangoDbProp) {
        this.tangoDbProp = tangoDbProp;
    }

    public void setTangoAccessProp(String tangoAccessProp) {
        this.tangoAccessProp = tangoAccessProp;
    }

    public void setTomcatPort(int tomcatPort) {
        this.tomcatPort = tomcatPort;
    }

    public void setTomcatAuthConfigurationClass(String tomcatAuthConfig) {
        this.tomcatAuthConfigurationClass = tomcatAuthConfig;
    }
}
