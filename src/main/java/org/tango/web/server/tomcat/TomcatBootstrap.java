package org.tango.web.server.tomcat;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tango.TangoRestServer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 9/18/17
 */
public class TomcatBootstrap {
    private final Logger logger = LoggerFactory.getLogger(TomcatBootstrap.class);

    public static final String WEBAPP_WAR = "webapp.war";

    private final int port;
    private final Path baseDir;
    private final AuthConfiguration authConfiguration;
    private final WebappConfiguration webappConfiguration;


    public TomcatBootstrap(int port,
                           Path baseDir,
                           AuthConfiguration authConfiguration, WebappConfiguration webappConfiguration) {
        this.port = port;
        this.baseDir = baseDir;

        this.authConfiguration = authConfiguration;
        this.webappConfiguration = webappConfiguration;
    }

    public static Path initializeBaseDir() {
        Path tomcatBaseDir;
        try(InputStream webapp = TangoRestServer.class.getResourceAsStream("/webapp.war")) {
            tomcatBaseDir = Files.createTempDirectory(
                    Paths.get(System.getProperty("user.dir")),"tomcat_");
            Files.createDirectory(tomcatBaseDir.resolve("webapps"));

            Files.copy(webapp, tomcatBaseDir.resolve(WEBAPP_WAR), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return tomcatBaseDir;
    }

    private Tomcat createTomcat(int port, String baseDir) {
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(port);
        tomcat.setBaseDir(baseDir);

        authConfiguration.configure(tomcat);
        webappConfiguration.configure(tomcat);
        return tomcat;
    }

    public Tomcat bootstrap() {
        logger.debug("Configure tomcat for device");

        Tomcat tomcat = createTomcat(port, baseDir.toAbsolutePath().toString());

        logger.debug("Starting tomcat of device...");
        try {
            tomcat.start();
            logger.debug("Done.");
            return tomcat;
        } catch (LifecycleException e) {
            throw new RuntimeException(e);
        }
    }
}
