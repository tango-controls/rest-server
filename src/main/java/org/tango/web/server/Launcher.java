package org.tango.web.server;

import hzg.wpn.tango.client.proxy.TangoProxyException;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 23.05.14
 */
public class Launcher implements ServletContextListener {
    public static final String TANGO_HOST = "TANGO_HOST";
    public static final String TANGO_LOCALHOST = "localhost:10000";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        String tangoHost = System.getenv(TANGO_HOST);
        if (tangoHost == null) tangoHost = TANGO_LOCALHOST;

        try {
            DatabaseDs db = new DatabaseDs(tangoHost);

            sce.getServletContext().setAttribute(DatabaseDs.TANGO_DB, db);

            DeviceMapper mapper = new DeviceMapper(db);

            sce.getServletContext().setAttribute(DeviceMapper.TANGO_MAPPER, mapper);

            System.out.println("MTango is initialized.");
        } catch (TangoProxyException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("MTango is destroyed.");
    }
}