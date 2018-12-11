package org.tango.web.server.tomcat;

import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.AprLifecycleListener;
import org.apache.catalina.startup.Tomcat;
import org.apache.coyote.http11.Http11AprProtocol;
import org.apache.coyote.http2.Http2Protocol;
import org.apache.tomcat.util.net.SSLHostConfig;
import org.apache.tomcat.util.net.SSLHostConfigCertificate;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 12/11/18
 */
public class Http2Configuration implements TomcatConfiguration {
    private final String keyFile;
    private final String certFile;

    public Http2Configuration(String certFile, String keyFile) {
        this.certFile = certFile;
        this.keyFile = keyFile;
    }

    public void configure(Tomcat tomcat){
        //preserve HTTP/1.1 connector on
        Connector http11connector = tomcat.getConnector();
        tomcat.setConnector(http11connector);

        Connector connector = new Connector(Http11AprProtocol.class.getName());

        connector.addLifecycleListener(new AprLifecycleListener());

        connector.setPort(http11connector.getPort() + 40);
        connector.setSecure(true);
        connector.setScheme("https");
        connector.setAttribute("SSLEnabled", "true");
        SSLHostConfig sslHostConfig = new SSLHostConfig();
        SSLHostConfigCertificate cert =
                new SSLHostConfigCertificate(sslHostConfig, SSLHostConfigCertificate.Type.RSA);
        cert.setCertificateFile(certFile);
        cert.setCertificateKeyFile(keyFile);
        sslHostConfig.addCertificate(cert);
        connector.addSslHostConfig(sslHostConfig);

        connector.addUpgradeProtocol(new Http2Protocol());

        tomcat.setConnector(connector);
    }
}
