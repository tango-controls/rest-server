package org.tango;

import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevSource;
import fr.esrf.Tango.DevState;
import fr.esrf.Tango.DevVarLongStringArray;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tango.client.ez.proxy.TangoProxy;
import org.tango.client.ez.proxy.TangoProxyException;
import org.tango.server.ServerManager;
import org.tango.server.ServerManagerUtils;
import org.tango.server.annotation.*;
import org.tango.web.server.TangoProxyPool;
import org.tango.web.server.tomcat.AccessLogConfiguration;
import org.tango.web.server.tomcat.AuthConfiguration;
import org.tango.web.server.tomcat.TomcatBootstrap;
import org.tango.web.server.tomcat.WebappConfiguration;

import javax.servlet.ServletException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 14.12.2015
 */
@Device(transactionType = TransactionType.NONE)
public class TangoRestServer {

    public static final String TANGO_ACCESS = "TANGO_ACCESS";
    public static final String TOMCAT_PORT_PROPERTY = "TOMCAT_PORT";
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
    @DeviceProperty(name = TANGO_ACCESS, defaultValue = DEFAULT_ACCESS_CONTROL)
    private String tangoAccessControlProperty;
    @DeviceProperty(name = TOMCAT_PORT_PROPERTY, defaultValue = "10001")
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
        Path baseDir = TomcatBootstrap.initializeBaseDir();

        tomcat = new TomcatBootstrap(tomcatPort, baseDir,
                new AuthConfiguration(tomcatAuthMethod, tomcatUsers, tomcatPasswords),
                new WebappConfiguration(baseDir.toAbsolutePath().toString()),
                new AccessLogConfiguration()).bootstrap();
        setStatus(String.format("TangoRestServer ver=%s\n Running tomcat on port[%d] ", getVersion(), tomcatPort));
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

        tangoAccessControlProperty = System.getProperty(TANGO_ACCESS, tangoAccessControlProperty);
        logger.debug("TANGO_ACCESS={}", tangoAccessControlProperty);
        System.setProperty(TANGO_ACCESS, tangoAccessControlProperty);

        tomcatPort = Integer.parseInt(System.getProperty(TOMCAT_PORT_PROPERTY, Integer.toString(tomcatPort)));
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
