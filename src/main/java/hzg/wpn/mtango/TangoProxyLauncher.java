package hzg.wpn.mtango;

import hzg.wpn.tango.client.proxy.TangoProxyException;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 23.05.14
 */
public class TangoProxyLauncher implements ServletContextListener {
    public static final String TANGO_LOCALHOST = "localhost:10000";
    public static final String TANGO_DB = "tango.db";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        String tangoHost = System.getenv("TANGO_HOST");
        if(tangoHost == null) tangoHost = TANGO_LOCALHOST;

        try {
            DatabaseDs db = new DatabaseDs(tangoHost);

            sce.getServletContext().setAttribute(TANGO_DB,db);

            System.out.println("TangoProxy is initialized.");
        } catch (TangoProxyException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("TangoProxy is destroyed.");
    }
}
