package org.tango.web.server.tomcat;

import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.valves.AccessLogValve;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 9/19/17
 */
public class AccessLogConfiguration {

    public void configure(Tomcat tomcat) {
        AccessLogValve accessLogValve = new AccessLogValve();
        Path dir = Paths.get(
                System.getProperty("LOG_HOME", System.getProperty("user.dir")));

        accessLogValve.setDirectory(dir.toAbsolutePath().toString());
        accessLogValve.setPattern("common");
        accessLogValve.setSuffix(".log");
        accessLogValve.setRotatable(true);

        tomcat.getHost().getPipeline().addValve(accessLogValve);
    }
}
