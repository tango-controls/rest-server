package org.tango.rest.tomcat;

import org.apache.catalina.startup.Tomcat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 9/18/17
 */
public class WebappConfiguration implements TomcatConfiguration {
    private final Logger logger = LoggerFactory.getLogger(WebappConfiguration.class);

    private final String webappPath;

    public WebappConfiguration(String webappPath) {
        this.webappPath = webappPath;
    }

    public void configure(Tomcat tomcat) {
        logger.debug("Add webapp[tango] tomcat for device");
        org.apache.catalina.Context context = tomcat.addWebapp("/tango", webappPath);
    }
}
