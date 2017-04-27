package org.tango;

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
import org.tango.web.server.TangoProxyPool;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
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
    public static final String DEFAULT_ACCESS_CONTROL = "none";//"sys/access_control/1";
    public static final String TANGO_INSTANCE = "tango.rest.server.instance";
    public static final String DEFAULT_AUTH_CLASS = "plain";
    // descriptions
    public static final String CACHE_ENABLED_DESC = "Enables/disables client and server cache. Client cache means adding HTTP request headers.";

    public static final String ATTR_VAL_DESC = "Defines HTTP response expiration header value for dynamic values and for how long server will keep the value. (aka attribute value)";
    public static final String STATIC_VAL_DESC = "Defines HTTP response expiration header value for static values and for how long server will keep the value. (aka list of the devices in a db).";
    public static final String SYS_DATABASE_2 = "sys/database/2";
    public static final String TANGO_LOCALHOST = "localhost:10000";
    public final TangoProxyPool hostsPool = new TangoProxyPool();
    public final TangoProxyPool proxyPool = new TangoProxyPool();
    private final Logger logger = LoggerFactory.getLogger(TangoRestServer.class);
    @DeviceProperty(name = TANGO_DB_NAME, defaultValue = SYS_DATABASE_2)
    private String tangoDbNameProp;
    @DeviceProperty(name = TANGO_DB, defaultValue = SYS_DATABASE_2)
    private String tangoDbProp;
    @DeviceProperty(name = TANGO_ACCESS, defaultValue = DEFAULT_ACCESS_CONTROL)
    private String tangoAccessControlProperty;
    @DeviceProperty(name = TOMCAT_PORT, defaultValue = "10001")
    private int tomcatPort = 10001;
    @DeviceProperty(name = TOMCAT_AUTH_CONFIG, defaultValue = DEFAULT_AUTH_CLASS)
    private String tomcatAuthMethod;
    @DeviceProperty(name = TOMCAT_USERS, defaultValue = {"ingvord", "tango-cs"})
    private String[] tomcatUsers;
    @DeviceProperty(name = TOMCAT_PASSWORDS, defaultValue = {"test", "tango"})
    private String[] tomcatPasswords;
    @DeviceProperty(name = TOMCAT_CACHE_SIZE, defaultValue = "100")
    private int tomcatCacheSize;
    @Attribute(isMemorized = true)
    @AttributeProperties(description = CACHE_ENABLED_DESC)
    private volatile boolean cacheEnabled;
    @Attribute(isMemorized = true)
    @AttributeProperties(unit = "millis", description = ATTR_VAL_DESC)
    private volatile long dynamicValueExpirationDelay;
    @Attribute(isMemorized = true)
    @AttributeProperties(unit = "millis", description = STATIC_VAL_DESC)
    private volatile long staticValueExpirationDelay;
    @State
    private DevState state = DevState.OFF;
    @Status
    private String status;
    private Tomcat tomcat;
    private AuthConfiguration authConfiguration;

    public static void main(String[] args) throws Exception {
        String instance = args[0];
        System.setProperty(TANGO_INSTANCE, instance);
        ServerManager.getInstance().start(args, TangoRestServer.class);//calls init method

        List<TangoRestServer> tangoRestServers =
                ServerManagerUtils.getBusinessObjects(instance, TangoRestServer.class);

        if (tangoRestServers.size() > 1)
            throw new IllegalStateException("TangoRestServer must have exactly one device! Actually has: " + tangoRestServers.size());

        tangoRestServers.get(0).startTomcat();//calls Launcher.onContextCreated, i.e. creates TangoContext using properties set in init method
    }

    public static String getVersion() {
        return TangoRestServer.class.getPackage().getImplementationVersion();
    }

    private void startTomcat() throws Exception {
        logger.debug("Configure tomcat for device");

        Path tomcatBaseDir;
        try {
            tomcatBaseDir = Files.createTempDirectory("tomcat_");
            Files.createDirectory(tomcatBaseDir.resolve("webapps"));
        } catch (IOException e) {
            logger.error("Failed to create tomcat temp dir", e);
            throw e;
        }
        try {
            InputStream webapp = TangoRestServer.class.getResourceAsStream("/webapp.war");


            Files.copy(webapp, tomcatBaseDir.resolve(WEBAPP_WAR), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            logger.error(String.format(
                    "Failed to extract webapp.war to the temp dir %s", tomcatBaseDir.toAbsolutePath().toString()), e);
            throw e;
        }

        tomcat = new Tomcat();
        tomcat.setPort(tomcatPort);
        tomcat.setBaseDir(tomcatBaseDir.toAbsolutePath().toString());

        logger.debug("Add webapp[tango] tomcat for device");
        org.apache.catalina.Context context =
                tomcat.addWebapp("tango", tomcatBaseDir.resolve(WEBAPP_WAR).toAbsolutePath().toString());

        WebappLoader loader =
                new WebappLoader(Thread.currentThread().getContextClassLoader());
        loader.setDelegate(true);
        context.setLoader(loader);

        logger.debug("Configure tomcat auth for device");
        authConfiguration.configure(tomcat);

        logger.debug("Start tomcat of device");
        tomcat.start();
        setStatus(String.format("TangoRestServer ver=%s\n Running tomcat on port[%d] ", getVersion(), tomcatPort));

        logger.debug("Done.");
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
        tangoAccessControlProperty = System.getProperty(TANGO_ACCESS, tangoAccessControlProperty);
        logger.debug("TANGO_ACCESS={}", tangoAccessControlProperty);
        System.setProperty(TANGO_ACCESS, tangoAccessControlProperty);

        try {
            TangoProxy dbProxy = TangoProxies.newDeviceProxyWrapper(tangoDbProp);
            DatabaseDs databaseDs = new DatabaseDs(dbProxy);
            String tangoDbHost = dbProxy.toDeviceProxy().get_tango_host();
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
        Collection<String> result = proxyPool.proxies();
        return result.toArray(new String[result.size()]);
    }

    @Command(inTypeDesc = "deviceName->value")
    public void setProxiesSource(DevVarLongStringArray input) throws Exception {
        String[] svalue = input.svalue;
        for (int i = 0, svalueLength = svalue.length; i < svalueLength; i++) {
            String device = svalue[i];
            TangoProxy proxy = proxyPool.getProxy(device);
            DevSource new_src = DevSource.from_int(input.lvalue[i]);
            proxy.toDeviceProxy().set_source(new_src);
            proxyPool.tangoProxyCreationPolicies.put(proxy.getName(), new TangoProxyPool.TangoProxyCreationPolicy(new_src));
        }
    }

    public boolean isCacheEnabled() {
        return cacheEnabled;
    }

    public void setCacheEnabled(boolean v) {
        cacheEnabled = v;
    }

//    @Attribute(isMemorized = true)
//    @AttributeProperties(unit = "minutes")
//    public long getProxyKeepAliveDelay() {
//        return ctx.tangoProxyKeepAliveDelay;
//    }
//
//    @Attribute(isMemorized = true)
//    @AttributeProperties(unit = "minutes")
//    public void setProxyKeepAliveDelay(long v) {
//        ctx.tangoProxyKeepAliveDelay = v;
//    }

    public long getDynamicValueExpirationDelay() {
        return dynamicValueExpirationDelay;
    }

    public void setDynamicValueExpirationDelay(long v) {
        dynamicValueExpirationDelay = v;
    }


    public long getStaticValueExpirationDelay() {
        return staticValueExpirationDelay;
    }

    public void setStaticValueExpirationDelay(long v) {
        staticValueExpirationDelay = v;
    }

    public void setTangoDbNameProp(String tangoDbName) {
        this.tangoDbNameProp = tangoDbName;
    }

    public void setTangoDbProp(String tangoDbProp) {
        this.tangoDbProp = tangoDbProp;
    }

    public String getTangoAccessControlProperty() {
        return this.tangoAccessControlProperty;
    }

    public void setTangoAccessControlProperty(String tangoAccessControlProperty) {
        this.tangoAccessControlProperty = tangoAccessControlProperty;
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

    public int getTomcatCacheSize() {
        return tomcatCacheSize;
    }

    public void setTomcatCacheSize(int tomcatCacheSize) {
        this.tomcatCacheSize = tomcatCacheSize;
    }

    public TangoProxy getHostProxy(String host, String port, String dbName) throws TangoProxyException {
        return hostsPool.getProxy("tango://" + host + ":" + port + "/" + dbName);
    }
}
