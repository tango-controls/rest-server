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
import org.tango.web.server.AuthConfiguration;
import org.tango.web.server.DatabaseDs;
import org.tango.web.server.TangoContext;
import org.tango.web.server.TangoProxyCreationPolicy;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 14.12.2015
 */
@Device(transactionType = TransactionType.NONE)
public class TangoRestServer {
    public static final String WEBAPP_WAR = "webapp.war";
    public static final String TANGO_DB_NAME = "TANGO_DB_NAME";
    public static final String TANGO_DB = "TANGO_DB";
    public static final String TANGO_ACCESS = "TANGO_ACCESS";
    public static final String TOMCAT_PORT = "TOMCAT_PORT";
    public static final String TOMCAT_AUTH_CONFIG = "TOMCAT_AUTH_METHOD";
    public static final String TOMCAT_USERS = "TOMCAT_USERS";
    public static final String TOMCAT_PASSWORDS = "TOMCAT_PASSWORDS";
    public static final String TOMCAT_CACHE_SIZE = "TOMCAT_CACHE_SIZE";
    public static final String SYS_ACCESS_CONTROL_1 = "sys/access_control/1";
    public static final String TANGO_INSTANCE = "tango.rest.server.instance";
    public static final String DEFAULT_AUTH_CLASS = "plain";
    // descriptions
    public static final String CACHE_ENABLED_DESC = "Enables/disables client and server cache. Client cache means adding HTTP request headers.";

    public static final String ATTR_VAL_DESC = "Defines HTTP response expiration header value for attribute values.";
    public static final String STATIC_VAL_DESC = "Defines HTTP response expiration header value for static values, aka list of the devices in a db (defined in the source code).";
    private static final Logger logger = LoggerFactory.getLogger(TangoRestServer.class);

    private final TangoContext ctx = new TangoContext();
    @DeviceProperty(name = TANGO_DB_NAME, defaultValue = TangoContext.SYS_DATABASE_2)
    private String tangoDbNameProp;
    @DeviceProperty(name = TANGO_DB, defaultValue = TangoContext.SYS_DATABASE_2)
    private String tangoDbProp;
    @DeviceProperty(name = TANGO_ACCESS, defaultValue = SYS_ACCESS_CONTROL_1)
    private String tangoAccessProp;
    @DeviceProperty(name = TOMCAT_PORT, defaultValue = "10001")
    private int tomcatPort = 10001;
    @DeviceProperty(name = TOMCAT_AUTH_CONFIG, defaultValue = DEFAULT_AUTH_CLASS)
    private String tomcatAuthMethod;
    @DeviceProperty(name = TOMCAT_USERS, defaultValue = {"ingvord", "tango-cs"})
    private String[] tomcatUsers;
    @DeviceProperty(name = TOMCAT_PASSWORDS, defaultValue = {"test", "tango"})
    private String[] tomcatPasswords;
    @DeviceProperty(name = TOMCAT_CACHE_SIZE, defaultValue = "1000")
    private int tomcatCacheSize;

    @State
    private DevState state = DevState.OFF;
    @Status
    private String status;
    private String tangoDbHost;
    private Tomcat tomcat;
    private AuthConfiguration authConfiguration;

    public static void main(String[] args) throws Exception {
        String instance = args[0];
        System.setProperty(TANGO_INSTANCE, instance);
        ServerManager.getInstance().start(args, TangoRestServer.class);//calls init method

        startTomcat(instance);//calls Launcher.onContextCreated, i.e. creates TangoContext using properties set in init method
    }

    private static void startTomcat(String instance) throws Exception {
        List<TangoRestServer> tangoRestServers = ServerManagerUtils.getBusinessObjects(instance, TangoRestServer.class);

        if(tangoRestServers.size() > 1)
            throw new IllegalStateException("TangoRestServer must have exactly one device! Actually has: " + tangoRestServers.size());


        for (TangoRestServer tangoRestServer : tangoRestServers) {
            logger.trace("Configure tomcat for device");

            Path tomcatBaseDir;
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

            tangoRestServer.tomcat = new Tomcat();
            tangoRestServer.tomcat.setPort(tangoRestServer.tomcatPort);
            tangoRestServer.tomcat.setBaseDir(tomcatBaseDir.toAbsolutePath().toString());

            logger.trace("Add webapp[tango] tomcat for device");
            org.apache.catalina.Context context =
                    tangoRestServer.tomcat.addWebapp("tango", tomcatBaseDir.resolve(WEBAPP_WAR).toAbsolutePath().toString());

            WebappLoader loader =
                    new WebappLoader(Thread.currentThread().getContextClassLoader());
            loader.setDelegate(true);
            context.setLoader(loader);

            logger.trace("Configure tomcat auth for device");
            tangoRestServer.authConfiguration.configure(tangoRestServer.tomcat);

            logger.trace("Start tomcat of device");
            tangoRestServer.tomcat.start();
            tangoRestServer.setStatus("Running tomcat on port " + tangoRestServer.tomcatPort);
        }

        logger.trace("Done.");
    }

    public DevState getState() {
        return state;
    }

    public void setState(DevState state) {
        this.state = state;
    }

    @Init
    @StateMachine(endState = DeviceState.ON)
    public void init() throws DevFailed, ServletException, TangoProxyException, LifecycleException {
        logger.info("Initializing TangoRestServer device...");
        tangoDbNameProp = System.getProperty(TANGO_DB, tangoDbNameProp);
        logger.debug("TANGO_DB_NAME={}", tangoDbNameProp);
        tangoDbProp = System.getProperty(TANGO_DB, tangoDbProp);
        logger.debug("TANGO_DB={}", tangoDbProp);
        System.setProperty(TANGO_DB, tangoDbProp);
        tangoAccessProp = System.getProperty(TANGO_ACCESS, tangoAccessProp);
        logger.debug("TANGO_ACCESS={}", tangoAccessProp);
        System.setProperty(TANGO_ACCESS, tangoAccessProp);

        try {
            TangoProxy dbProxy = TangoProxies.newDeviceProxyWrapper(tangoDbProp);
            DatabaseDs databaseDs = new DatabaseDs(dbProxy);
            tangoDbHost = dbProxy.toDeviceProxy().get_tango_host();
            logger.debug("TANGO_DB_HOST={}", tangoDbHost);
            if (tangoDbHost.endsWith("10000")) tangoDbHost = tangoDbHost.substring(0, tangoDbHost.indexOf(':'));
        } catch (TangoProxyException e) {
            logger.warn("Failed to create DatabaseProxy! Ignore if in -nodb mode...", e);
        }



        tomcatPort = Integer.parseInt(System.getProperty(TOMCAT_PORT, Integer.toString(tomcatPort)));

        authConfiguration = new AuthConfiguration(tomcatAuthMethod, tomcatUsers, tomcatPasswords);
    }

    @Delete
    @StateMachine(endState = DeviceState.OFF)
    public void delete() throws LifecycleException {
        if (tomcat != null) {
            tomcat.stop();
            tomcat.destroy();
        }
    }

    @Attribute
    public String[] getAliveProxies() throws Exception {
        List<String> result = Lists.transform(
                ctx.proxyPool.proxies(),
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
            TangoProxy proxy = ctx.proxyPool.getProxy(device);
            DevSource new_src = DevSource.from_int(input.lvalue[i]);
            proxy.toDeviceProxy().set_source(new_src);
            ctx.tangoProxyCreationPolicies.put(proxy.getName(), new TangoProxyCreationPolicy(new_src));
        }
    }

    @Attribute(isMemorized = true)
    @AttributeProperties(description = CACHE_ENABLED_DESC)
    public boolean getCacheEnabled() {
        return ctx.isCacheEnabled;
    }

    @Attribute(isMemorized = true)
    @AttributeProperties(description = CACHE_ENABLED_DESC)
    public void setCacheEnabled(boolean v) {
        ctx.isCacheEnabled = v;
    }

    @Attribute(isMemorized = true)
    @AttributeProperties(unit = "minutes")
    public long getProxyKeepAliveDelay(){
        return ctx.tangoProxyKeepAliveDelay;
    }

    @Attribute(isMemorized = true)
    @AttributeProperties(unit = "minutes")
    public void setProxyKeepAliveDelay(long v) {
        ctx.tangoProxyKeepAliveDelay = v;
    }

    @Attribute(isMemorized = true)
    @AttributeProperties(unit = "millis", description = ATTR_VAL_DESC)
    public long getAttributeValueExpirationDelay() {
        return ctx.attributeValueExpirationDelay;
    }

    @Attribute(isMemorized = true)
    @AttributeProperties(unit = "millis", description = ATTR_VAL_DESC)
    public void setAttributeValueExpirationDelay(long v) {
        ctx.attributeValueExpirationDelay = v;
    }


    @Attribute(isMemorized = true)
    @AttributeProperties(unit = "millis", description = STATIC_VAL_DESC)
    public long getStaticValueExpirationDelay() {
        return ctx.staticDataExpirationDelay;
    }

    @Attribute(isMemorized = true)
    @AttributeProperties(unit = "millis", description = STATIC_VAL_DESC)
    public void setStaticValueExpirationDelay(long v) {
        ctx.staticDataExpirationDelay = v;
    }

    public void setTangoDbNameProp(String tangoDbName) {
        this.tangoDbNameProp = tangoDbName;
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

    public void setTomcatAuthMethod(String tomcatAuthConfig) {
        this.tomcatAuthMethod = tomcatAuthConfig;
    }

    public String[] getTomcatUsers() {
        return tomcatUsers;
    }

    public void setTomcatUsers(String[] tomcatUsers) {
        this.tomcatUsers = tomcatUsers;
    }

    public String[] getTomcatPasswords() {
        return tomcatPasswords;
    }

    public void setTomcatPasswords(String[] tomcatPasswords) {
        this.tomcatPasswords = tomcatPasswords;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public TangoContext getCtx(){
        return ctx;
    }

    public int getTomcatCacheSize() {
        return tomcatCacheSize;
    }

    public void setTomcatCacheSize(int tomcatCacheSize) {
        this.tomcatCacheSize = tomcatCacheSize;
    }
}
