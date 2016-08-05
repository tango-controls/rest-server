package org.tango.web.server;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tango.TangoRestServer;
import org.tango.client.ez.proxy.TangoProxies;
import org.tango.client.ez.proxy.TangoProxy;
import org.tango.client.ez.proxy.TangoProxyException;
import org.tango.server.ServerManager;
import org.tango.server.ServerManagerUtils;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.List;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 23.05.14
 */
public class Launcher implements ServletContextListener {
    private final Logger logger = LoggerFactory.getLogger(Launcher.class);

    public static final String TANGO_HOST = "TANGO_HOST";
    public static final String TANGO_LOCALHOST = "localhost:10000";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        String tangoHost = System.getProperty(TANGO_HOST, System.getenv(TANGO_HOST));
        if (tangoHost == null) System.setProperty(TANGO_HOST, tangoHost = TANGO_LOCALHOST);
        logger.info("TANGO_HOST={}", tangoHost);

        logger.info("TANGO_INSTANCE={}", System.getProperty(TangoRestServer.TANGO_INSTANCE,
                sce.getServletContext().getInitParameter(TangoRestServer.TANGO_INSTANCE)));

        try {
            startTangoServer();

            TangoContext context = new TangoContext();
            context.tangoHost = tangoHost;

            String tangoDb = System.getProperty(TangoRestServer.TANGO_DB, TangoRestServer.SYS_DATABASE_2);
            String tangoDbName = System.getProperty(TangoRestServer.TANGO_DB_NAME, TangoRestServer.SYS_DATABASE_2);
            context.tangoDbName = tangoDbName;

            TangoProxy dbProxy = TangoProxies.newDeviceProxyWrapper(tangoDb);
            DatabaseDs db = new DatabaseDs(dbProxy);
            context.databaseDs = db;


            sce.getServletContext().setAttribute(DatabaseDs.TANGO_DB, db);

            DeviceMapper mapper = new DeviceMapper(context);
            context.deviceMapper = mapper;

            sce.getServletContext().setAttribute(DeviceMapper.TANGO_MAPPER, mapper);

            String accessControlProp = System.getProperty(TangoRestServer.TANGO_ACCESS, TangoRestServer.SYS_ACCESS_CONTROL_1);

            TangoProxy accessCtlProxy = TangoProxies.newDeviceProxyWrapper(accessControlProp);
            AccessControl accessControl = new AccessControl(accessCtlProxy);

            sce.getServletContext().setAttribute(AccessControl.TANGO_ACCESS, accessControl);
            context.accessControl = accessControl;

            sce.getServletContext().setAttribute(TangoContext.TANGO_CONTEXT, context);

            setTangoRestServerContext(context);

            logger.info("MTango is initialized.");
        } catch (TangoProxyException e) {
            logger.error("MTango has failed to initialize: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void startTangoServer() {
        String instance = System.getProperty(TangoRestServer.TANGO_INSTANCE, "development");

        ServerManager.getInstance().start(new String[]{instance}, TangoRestServer.class);
    }

    private void setTangoRestServerContext(TangoContext ctx){
        String instance = System.getProperty(TangoRestServer.TANGO_INSTANCE, "development");

        List<TangoRestServer> tangoRestServers = ServerManagerUtils.getBusinessObjects(instance, TangoRestServer.class);
        for (TangoRestServer tangoRestServer : tangoRestServers) {
            tangoRestServer.ctx = ctx;
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        DeviceMapper.scheduler.shutdownNow();
        logger.info("MTango is destroyed.");
    }
}
