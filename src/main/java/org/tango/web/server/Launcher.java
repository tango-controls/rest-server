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

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        String tangoHost = System.getProperty(TANGO_HOST, System.getenv(TANGO_HOST));
        if (tangoHost == null) System.setProperty(TANGO_HOST, tangoHost = TangoContext.TANGO_LOCALHOST);
        logger.info("TANGO_HOST={}", tangoHost);

        logger.info("TANGO_INSTANCE={}", System.getProperty(TangoRestServer.TANGO_INSTANCE,
                sce.getServletContext().getInitParameter(TangoRestServer.TANGO_INSTANCE)));

        try {
            startTangoServer();//calls TangoRestServer.init - sets System properties from Device properties

            TangoContext context = new TangoContext();
            context.tangoHost = tangoHost;

            context.tangoDbName = System.getProperty(TangoRestServer.TANGO_DB_NAME, TangoContext.SYS_DATABASE_2);

            context.tangoDb = System.getProperty(TangoRestServer.TANGO_DB, "tango://" + context.tangoHost + "/" + context.tangoDbName);
            //TODO replace with TangoContext.hostsPool
            DatabaseDs db = new DatabaseDs(context.hostsPool.getProxy(context.tangoDb));//puts proxy instance into cache
            sce.getServletContext().setAttribute(DatabaseDs.TANGO_DB, db);//for backward compatability

            String accessControlProp = System.getProperty(TangoRestServer.TANGO_ACCESS, TangoRestServer.SYS_ACCESS_CONTROL_1);

            TangoProxy accessCtlProxy = TangoProxies.newDeviceProxyWrapper(accessControlProp);
            AccessControl accessControl = new AccessControl(accessCtlProxy);

            sce.getServletContext().setAttribute(AccessControl.TANGO_ACCESS, accessControl);

            sce.getServletContext().setAttribute(TangoContext.TANGO_CONTEXT, context);

            setTangoRestServerContext(context);

            logger.info("TangoRestServer is initialized.");
        } catch (TangoProxyException e) {
            logger.error("TangoRestServer has failed to initialize: {}", e.getMessage());
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
        logger.info("TangoRestServer has been destroyed!");
    }
}
