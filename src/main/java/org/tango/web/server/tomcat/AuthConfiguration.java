package org.tango.web.server.tomcat;

import de.hzg.wpi.utils.authorization.Kerberos;
import de.hzg.wpi.utils.authorization.PlainText;
import org.apache.catalina.startup.Tomcat;
import org.jboss.resteasy.specimpl.MultivaluedMapImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tango.TangoRestServer;

import javax.ws.rs.core.MultivaluedMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 04.02.2016
 */
public class AuthConfiguration {
    private final Logger logger = LoggerFactory.getLogger(AuthConfiguration.class);

    public static final String[] DEFAULT_ROLES = {"desy-user", "mtango-rest", "mtango-groovy"};

    private Map<String, String> users = new HashMap<>();
    private MultivaluedMap<String, String> roles = new MultivaluedMapImpl<>();
    private String authMethod;

    public AuthConfiguration(String authMethod, String[] users, String[] passwords) {
        this.authMethod = authMethod;
        for (int i = 0; i < users.length; ++i) {
            this.users.put(users[i], passwords[i]);
            this.roles.put(users[i], Arrays.asList(DEFAULT_ROLES));
        }
    }

    public void configure(Tomcat tomcat){
        logger.debug("Configure tomcat auth for device");
        switch (authMethod) {
            case "plain":
                PlainText plainText = new PlainText(tomcat, users, roles);
                plainText.configure();
                break;
            case "kerberos":
                Kerberos kerberos = new Kerberos(tomcat, TangoRestServer.class.getSimpleName());
                kerberos.configure();
                break;
        }
    }
}
