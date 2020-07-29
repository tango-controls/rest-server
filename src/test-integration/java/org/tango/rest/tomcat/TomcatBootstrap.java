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

package org.tango.rest.tomcat;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 9/18/17
 */
public class TomcatBootstrap {
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

    public Path initializeBaseDir() throws IOException {
        Path tomcatBaseDir = Files.createTempDirectory(
                baseDir, "tomcat_");
        FileUtils.forceDeleteOnExit(tomcatBaseDir.toFile());

        Files.createDirectory(tomcatBaseDir.resolve("webapps"));
        return tomcatBaseDir;
    }

    private Tomcat createTomcat(int port) throws IOException {
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(port);
        tomcat.setBaseDir(initializeBaseDir().toAbsolutePath().toString());


        http2Configuration.configure(tomcat);
        accessLogConfiguration.configure(tomcat);
        authConfiguration.configure(tomcat);
        webappConfiguration.configure(tomcat);
        return tomcat;
    }

    public Tomcat bootstrap() throws IOException {
        logger.debug("Configure tomcat for device");

        Tomcat tomcat = createTomcat(port);

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
