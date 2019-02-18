/*
 * Copyright 2019 Tango Controls
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tango.web.server;


import fr.esrf.Tango.DevFailed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tango.TangoRestServer;
import org.tango.client.ez.proxy.TangoProxies;
import org.tango.client.ez.proxy.TangoProxy;
import org.tango.client.ez.proxy.TangoProxyException;
import org.tango.client.ez.util.TangoUtils;
import org.tango.server.ServerManager;
import org.tango.server.ServerManagerUtils;
import org.tango.utils.DevFailedUtils;
import org.tango.web.server.attribute.EventBuffer;

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

        String instance = System.getProperty(TangoRestServer.TANGO_INSTANCE,
                sce.getServletContext().getInitParameter(TangoRestServer.TANGO_INSTANCE));
        logger.info("TANGO_INSTANCE={}", instance);

        try {
            startTangoServer(instance, sce.getServletContext());//calls TangoRestServer.init - sets System properties from Device properties

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
        TangoRestServer tangoRestServer = (TangoRestServer) servletContext.getAttribute(TangoRestServer.class.getName());

        if(tangoRestServer == null) throw new NullPointerException("tangoRestServer is null! Check initialization sequence...");

        String accessControlProp = tangoRestServer.getTangoAccessControlProperty();

        boolean skipAccessControl = accessControlProp.isEmpty() || "none".equalsIgnoreCase(accessControlProp);
        if (!skipAccessControl) {
            TangoProxy accessCtlProxy = TangoProxies.newDeviceProxyWrapper(accessControlProp);
            AccessControl accessControl = new AccessControl(accessCtlProxy);
            servletContext.setAttribute(AccessControl.class.getName(), accessControl);
        }

        servletContext.setAttribute(EventBuffer.class.getName(), new EventBuffer());
    }

    /**
     * NoOp if already started, i.e. in {@link TangoRestServer#main(String[])}
     *
     * Sets started TangoRestServer device instance as context parameter
     *
     * @param instance
     * @param context
     */
    private void startTangoServer(String instance, ServletContext context) {
        if(ServerManager.getInstance().isStarted()) return;

        ServerManager.getInstance().start(
                new String[]{System.getProperty(TangoRestServer.TANGO_INSTANCE, instance)}, TangoRestServer.class);

        List<TangoRestServer> tangoRestServers = ServerManagerUtils.getBusinessObjects(instance, TangoRestServer.class);
        if (tangoRestServers.size() > 1)
            throw new RuntimeException("This Tango server must have exactly one defined device.");
        context.setAttribute(TangoRestServer.class.getName(), tangoRestServers.get(0));
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        try {
            ServerManager.getInstance().stop();
            logger.info("TangoRestServer has been destroyed!");
        } catch (DevFailed devFailed) {
            logger.error("TangoRestServer failed to stop!", TangoUtils.convertDevFailedToException(devFailed));
        }
    }
}
