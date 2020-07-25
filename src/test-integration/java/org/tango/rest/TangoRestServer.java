package org.tango.rest;

import org.apache.catalina.startup.Tomcat;
import org.tango.rest.tomcat.*;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author ingvord
 * @since 25.07.2020
 */
public class TangoRestServer {
    public static void main(String[] args) throws Exception {
        Path baseDir = Paths.get("target/webapp");

        Tomcat tomcat = new TomcatBootstrap(10001, baseDir,
                new AuthConfiguration("plain", new String[]{"tango-cs"}, new String[]{"tango"}),
                new WebappConfiguration(baseDir.toAbsolutePath().toString()),
                new AccessLogConfiguration(),
                new Http2Configuration("/etc/ssl/certs/ssl-cert-snakeoil.pem", "/etc/ssl/private/ssl-cert-snakeoil.key"))
                .bootstrap();

        tomcat.getServer().await();
    }
}
