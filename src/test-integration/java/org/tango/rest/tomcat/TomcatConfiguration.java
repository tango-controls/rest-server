package org.tango.rest.tomcat;

import org.apache.catalina.startup.Tomcat;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 12/11/18
 */
public interface TomcatConfiguration {
    void configure(Tomcat tomcat);
}
