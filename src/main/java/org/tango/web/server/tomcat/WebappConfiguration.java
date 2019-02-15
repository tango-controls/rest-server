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

import org.apache.catalina.loader.WebappLoader;
import org.apache.catalina.startup.Tomcat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tango.TangoRestServer;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 9/18/17
 */
public class WebappConfiguration implements TomcatConfiguration {
    private final Logger logger = LoggerFactory.getLogger(WebappConfiguration.class);

    private final String webappPath;
    private final TangoRestServer tangoRestServer;

    public WebappConfiguration(String webappPath, TangoRestServer tangoRestServer) {
        this.webappPath = webappPath + "/webapp.war";
        this.tangoRestServer = tangoRestServer;
    }

    public void configure(Tomcat tomcat){
        logger.debug("Add webapp[tango] tomcat for device");
        org.apache.catalina.Context context = tomcat.addWebapp("tango", webappPath);

        WebappLoader loader =
                new WebappLoader(Thread.currentThread().getContextClassLoader());
        loader.setDelegate(true);
        context.setLoader(loader);
        context.getServletContext().setAttribute(TangoRestServer.class.getName(), tangoRestServer);
    }
}
