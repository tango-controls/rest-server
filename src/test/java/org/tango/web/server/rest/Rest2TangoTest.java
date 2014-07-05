package org.tango.web.server.rest;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.jboss.resteasy.client.jaxrs.ClientHttpEngine;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient4Engine;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Ingvord
 * @since 05.07.14
 */
public class Rest2TangoTest {
    private ClientHttpEngine engine;

    @Before
    public void before() {
        // Configure HttpClient to authenticate preemptively
// by prepopulating the authentication data cache.

// 1. Create AuthCache instance
        AuthCache authCache = new BasicAuthCache();

// 2. Generate BASIC scheme object and add it to the local auth cache
        AuthScheme basicAuth = new BasicScheme();
        authCache.put(new HttpHost("localhost:8080"), basicAuth);

// 3. Add AuthCache to the execution context
        BasicHttpContext localContext = new BasicHttpContext();
        localContext.setAttribute(ClientContext.AUTH_CACHE, authCache);

// 4. Create client executor and proxy
        DefaultHttpClient httpClient = new DefaultHttpClient();
        BasicCredentialsProvider basicCredentialsProvider = new BasicCredentialsProvider();
        basicCredentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials("ingvord", "test"));
        httpClient.setCredentialsProvider(basicCredentialsProvider);
        engine = new ApacheHttpClient4Engine(httpClient, localContext);
    }

    @Test
    public void testGetDevices() throws Exception {
        ResteasyClient cr = new ResteasyClientBuilder().httpEngine(engine).build();// Request("http://www.mastertheboss.com/");
        String result = cr.target("http://localhost:8080/mtango/rest/devices")
                .request().get(String.class);
        System.out.println(result);
    }

    @Test
    public void testGetDevice() throws Exception {

    }

    @Test
    public void testGetDeviceInfo() throws Exception {

    }

    @Test
    public void testGetDeviceAttributes() throws Exception {

    }

    @Test
    public void testGetDeviceCommands() throws Exception {

    }

    @Test
    public void testPutAttribute() throws Exception {

    }

    @Test
    public void testGetCommandOrAttributeInfo() throws Exception {

    }

    @Test
    public void testGetCommandOrAttribute() throws Exception {

    }

    @Test
    public void testGetCommand() throws Exception {

    }
}
