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

package org.tango.web.server.tomcat;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.io.FileUtils;
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
    public static final String WEBAPP_WAR = "webapp.war";
    private final Logger logger = LoggerFactory.getLogger(TomcatBootstrap.class);
    private final int port;
    private final Path baseDir;
    //TODO DI or mediator or builder
    private final AuthConfiguration authConfiguration;
    private final WebappConfiguration webappConfiguration;
    private final AccessLogConfiguration accessLogConfiguration;
    private final Http2Configuration http2Configuration;


    public TomcatBootstrap(int port,
                           Path baseDir,
                           AuthConfiguration authConfiguration, WebappConfiguration webappConfiguration, AccessLogConfiguration accessLogConfiguration, Http2Configuration http2Configuration) {
        this.port = port;
        this.baseDir = baseDir;

        this.authConfiguration = authConfiguration;
        this.webappConfiguration = webappConfiguration;
        this.accessLogConfiguration = accessLogConfiguration;
        this.http2Configuration = http2Configuration;
    }

    public static Path initializeBaseDir() {
        Path tomcatBaseDir;
        try(InputStream webapp = TangoRestServer.class.getResourceAsStream("/webapp.war")) {
            tomcatBaseDir = Files.createTempDirectory(
                    Paths.get(System.getProperty("user.dir")),"tomcat_");
            FileUtils.forceDeleteOnExit(tomcatBaseDir.toFile());

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


        http2Configuration.configure(tomcat);
        accessLogConfiguration.configure(tomcat);
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
