package org.tango.rest.tomcat;

import org.apache.catalina.startup.Tomcat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 9/18/17
 */
public class WebappConfiguration implements TomcatConfiguration {
    private final Logger logger = LoggerFactory.getLogger(WebappConfiguration.class);

    private final String webappPath;
    private final String webappName;

    public WebappConfiguration(String webappPath, String webappName) {
        this.webappPath = webappPath;
        this.webappName = webappName;
    }

    public void configure(Tomcat tomcat) {
        logger.debug("Add webapp[tango] tomcat for device");
        org.apache.catalina.Context context = tomcat.addWebapp("/tango", Paths.get(webappPath, webappName + ".war").toAbsolutePath().toString());
    }
}
