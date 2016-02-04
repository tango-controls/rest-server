package org.tango.web.server;

import org.apache.catalina.startup.Tomcat;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 04.02.2016
 */
public class PlainTextAuthConfiguration {

    public void configure(Tomcat tomcat){
        tomcat.addUser("tango-cs", "tango");
        tomcat.addRole("tango-cs", "mtango-rest");

        tomcat.addUser("ingvord", "test");
        tomcat.addRole("ingvord", "mtango-rest");
    }
}
