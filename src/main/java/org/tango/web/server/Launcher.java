package org.tango.web.server;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tango.TangoRestServer;
import org.tango.client.ez.proxy.TangoProxies;
import org.tango.client.ez.proxy.TangoProxy;
import org.tango.client.ez.proxy.TangoProxyException;
import org.tango.server.ServerManager;
import org.tango.server.ServerManagerUtils;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.List;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 23.05.14
 */
public class Launcher implements ServletContextListener {
    public static final String TANGO_HOST = "TANGO_HOST";
    private final Logger logger = LoggerFactory.getLogger(Launcher.class);

    //org.jboss.resteasy.spi.ResteasyProviderFactory
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        String tangoHost = getTangoHost();
        logger.info("TANGO_HOST={}", tangoHost);

        logger.info("TANGO_INSTANCE={}", System.getProperty(TangoRestServer.TANGO_INSTANCE,
                sce.getServletContext().getInitParameter(TangoRestServer.TANGO_INSTANCE)));

        try {
            startTangoServer();//calls TangoRestServer.init - sets System properties from Device properties

            initializeTangoServletContext(sce.getServletContext());

            logger.info("TangoRestServer servlet engine is initialized.");
        } catch (TangoProxyException e) {
            logger.error("TangoRestServer servlet engine has failed to initialize: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public String getTangoHost() {
        String tangoHost = System.getProperty(TANGO_HOST, System.getenv(TANGO_HOST));
        if (tangoHost == null) System.setProperty(TANGO_HOST, tangoHost = TangoRestServer.TANGO_LOCALHOST);
        return tangoHost;
    }

    private void initializeTangoServletContext(ServletContext servletContext) throws TangoProxyException {
        String instance = System.getProperty(TangoRestServer.TANGO_INSTANCE, "development");

        List<TangoRestServer> tangoRestServers = ServerManagerUtils.getBusinessObjects(instance, TangoRestServer.class);
        if (tangoRestServers.size() > 1)
            throw new RuntimeException("This Tango server must have exactly one defined device.");
        TangoRestServer tangoRestServer = tangoRestServers.get(0);

        servletContext.setAttribute(TangoRestServer.class.getName(), tangoRestServer);

        String accessControlProp = tangoRestServer.getTangoAccessControlProperty();

        boolean skipAccessControl = accessControlProp.isEmpty() || "none".equalsIgnoreCase(accessControlProp);
        if (!skipAccessControl) {
            TangoProxy accessCtlProxy = TangoProxies.newDeviceProxyWrapper(accessControlProp);
            AccessControl accessControl = new AccessControl(accessCtlProxy);
            servletContext.setAttribute(AccessControl.class.getName(), accessControl);
        }
    }

    /**
     * NoOp if already started, i.e. in {@link TangoRestServer#main(String[])}
     */
    private void startTangoServer() {
        String instance = System.getProperty(TangoRestServer.TANGO_INSTANCE, "development");

        ServerManager.getInstance().start(new String[]{instance}, TangoRestServer.class);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("TangoRestServer has been destroyed!");
    }
}
