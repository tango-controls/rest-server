package hzg.wpn.mtango;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 23.05.14
 */
public class TangoProxyLauncher implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        String tangoHost = System.getenv("TANGO_HOST");
        if(tangoHost == null) throw new IllegalStateException("env.TANGO_HOST is not defined - can not start application");

        System.out.println("TangoProxy is initialized.");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("TangoProxy is destroyed.");
    }
}
