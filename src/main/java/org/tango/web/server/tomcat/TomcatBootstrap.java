package org.tango.web.server.tomcat;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.coyote.http2.Http2Protocol;
import org.apache.tomcat.util.net.SSLHostConfig;
import org.apache.tomcat.util.net.SSLHostConfigCertificate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tango.TangoRestServer;

import java.io.File;
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
    public static final String WEBAPP_WAR = "webapp.war";
    private final Logger logger = LoggerFactory.getLogger(TomcatBootstrap.class);
    private final int port;
    private final Path baseDir;
    //TODO DI or mediator or builder
    private final AuthConfiguration authConfiguration;
    private final WebappConfiguration webappConfiguration;
    private final AccessLogConfiguration accessLogConfiguration;


    public TomcatBootstrap(int port,
                           Path baseDir,
                           AuthConfiguration authConfiguration, WebappConfiguration webappConfiguration, AccessLogConfiguration accessLogConfiguration) {
        this.port = port;
        this.baseDir = baseDir;

        this.authConfiguration = authConfiguration;
        this.webappConfiguration = webappConfiguration;
        this.accessLogConfiguration = accessLogConfiguration;
    }

    public static Path initializeBaseDir() {
        Path tomcatBaseDir;
        try(InputStream webapp = TangoRestServer.class.getResourceAsStream("/webapp.war")) {
            tomcatBaseDir = Files.createTempDirectory(
                    Paths.get(System.getProperty("user.dir")),"tomcat_");
            tomcatBaseDir.toFile().deleteOnExit();

            Files.createDirectory(tomcatBaseDir.resolve("webapps"));

            Files.copy(webapp, tomcatBaseDir.resolve(WEBAPP_WAR), StandardCopyOption.REPLACE_EXISTING);

            return tomcatBaseDir;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Tomcat createTomcat(int port, String baseDir) {
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(port);
        tomcat.setBaseDir(baseDir);

        Connector connector = tomcat.getConnector();
//        setupHttpsConnector(port, connector);

//        tomcat.getConnector().addUpgradeProtocol(new Http2Protocol());

        accessLogConfiguration.configure(tomcat);
        authConfiguration.configure(tomcat);
        webappConfiguration.configure(tomcat);
        return tomcat;
    }

    private void setupHttpsConnector(int port, Connector connector) {
        connector.setPort(port);
        connector.setSecure(true);
        connector.setScheme("https");
        connector.setAttribute("SSLEnabled", "true");
        SSLHostConfig sslHostConfig = new SSLHostConfig();
        SSLHostConfigCertificate cert =
                new SSLHostConfigCertificate(sslHostConfig, SSLHostConfigCertificate.Type.RSA);
        cert.setCertificateKeystoreFile(Paths.get("/storage/Projects/hzg.wpn.projects/mTango/mtangorest.server/keystore").toAbsolutePath().toString());
        cert.setCertificateKeystorePassword("igorkhokh@1234");
        cert.setCertificateKeyAlias("mtango");
        sslHostConfig.addCertificate(cert);
        connector.addSslHostConfig(sslHostConfig);
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
