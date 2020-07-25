package org.tango.rest.tomcat;

import de.hzg.wpi.utils.authorization.AuthorizationMechanism;
import de.hzg.wpi.utils.authorization.Kerberos;
import de.hzg.wpi.utils.authorization.Ldap;
import de.hzg.wpi.utils.authorization.PlainText;
import org.apache.catalina.startup.Tomcat;
import org.jboss.resteasy.specimpl.MultivaluedMapImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tango.rest.TangoRestServer;

import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 04.02.2016
 */
public class AuthConfiguration implements TomcatConfiguration {
    public static final String LDAP_PROPERTIES = "LDAP_PROPERTIES";
    public static final String[] DEFAULT_ROLES = {"desy-user", "mtango-rest", "mtango-groovy"};
    private final Logger logger = LoggerFactory.getLogger(AuthConfiguration.class);
    private Map<String, String> users = new HashMap<>();
    private MultivaluedMap<String, String> roles = new MultivaluedMapImpl<>();
    private AuthorizationMechanism authMethod;

    public AuthConfiguration(String authMethod, String[] users, String[] passwords) {
        for (int i = 0; i < users.length; ++i) {
            this.users.put(users[i], passwords[i]);
            this.roles.put(users[i], Arrays.asList(DEFAULT_ROLES));
        }
        this.authMethod = createAuthorizationMechanism(authMethod);
    }

    private AuthorizationMechanism createAuthorizationMechanism(String authMethod) {
        logger.debug("Configure tomcat auth {} for device", authMethod);
        try {
            switch (authMethod) {
                case "plain":
                    return new PlainText(users, roles);
                case "kerberos":
                    return new Kerberos(TangoRestServer.class.getSimpleName());
                case "ldap":
                    return new Ldap(System.getProperty(LDAP_PROPERTIES, "ldap.properties"));
                default:
                    throw new IllegalArgumentException("Unknown auth method - " + authMethod);
            }
        } catch (IOException e) {
            logger.error("Failed to create LDAP auth configuration!", e);
            throw new RuntimeException(e);
        }
    }

    public void configure(Tomcat tomcat) {
        logger.debug("Configuring tomcat auth for device...");
        authMethod.configure(tomcat);
        logger.debug("Done!");
    }
}
