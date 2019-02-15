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

import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.valves.AccessLogValve;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 9/19/17
 */
public class AccessLogConfiguration implements TomcatConfiguration {

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
