package org.tango.web.server.rest;

import com.google.gson.Gson;
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

import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * @author Ingvord
 * @since 05.07.14
 */
public class Rest2TangoTest {
    private final Gson gson = new Gson();

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
        ResteasyClient cr = new ResteasyClientBuilder().httpEngine(engine).build();
        String json = cr.target("http://localhost:8080/mtango/rest/device/sys/tg_test/1")
                .request().get(String.class);
        DeviceState result = gson.fromJson(json, DeviceState.class);
        assertTrue(result.state.equals("RUNNING"));
        assertTrue(result.status.equals("The device is in RUNNING state"));
    }

    @Test
    public void testGetDeviceInfo() throws Exception {
        ResteasyClient cr = new ResteasyClientBuilder().httpEngine(engine).build();
        String json = cr.target("http://localhost:8080/mtango/rest/device/sys/tg_test/1/info")
                .request().get(String.class);
        DeviceInfo result = gson.fromJson(json, DeviceInfo.class);
        assertTrue(result.name.equals("sys/tg_test/1"));
    }

    @Test
    public void testGetDeviceAttributes() throws Exception {
        ResteasyClient cr = new ResteasyClientBuilder().httpEngine(engine).build();
        String json = cr.target("http://localhost:8080/mtango/rest/device/sys/tg_test/1/attributes")
                .request().get(String.class);
        Response result = gson.fromJson(json, Response.class);
        assertTrue(((List<String>) result.argout).contains("long_scalar_w"));
    }

    @Test
    public void testGetDeviceCommands() throws Exception {
        ResteasyClient cr = new ResteasyClientBuilder().httpEngine(engine).build();
        String json = cr.target("http://localhost:8080/mtango/rest/device/sys/tg_test/1/commands")
                .request().get(String.class);
        Response result = gson.fromJson(json, Response.class);
        assertTrue(((List<String>) result.argout).contains("DevString"));
    }

    @Test
    public void testPutAttribute() throws Exception {
        ResteasyClient cr = new ResteasyClientBuilder().httpEngine(engine).build();
        cr.target("http://localhost:8080/mtango/rest/device/sys/tg_test/1/long_scalar_w=1234567")
                .request().put(null);
//        System.out.println(result);
    }

    @Test
    public void testGetAttributeInfo() throws Exception {
        ResteasyClient cr = new ResteasyClientBuilder().httpEngine(engine).build();
        String json = cr.target("http://localhost:8080/mtango/rest/device/sys/tg_test/1/long_scalar_w/info")
                .request().get(String.class);
        AttributeInfo result = gson.fromJson(json, AttributeInfo.class);
        assertTrue(result.name.equals("long_scalar_w"));
    }

    @Test
    public void testGetCommandInfo() throws Exception {
        ResteasyClient cr = new ResteasyClientBuilder().httpEngine(engine).build();
        String json = cr.target("http://localhost:8080/mtango/rest/device/sys/tg_test/1/DevString/info")
                .request().get(String.class);
        CommandInfo result = gson.fromJson(json, CommandInfo.class);
        assertTrue(result.cmd_name.equals("DevString"));
    }

    @Test
    public void testGetAttribute() throws Exception {
        ResteasyClient cr = new ResteasyClientBuilder().httpEngine(engine).build();
        String json = cr.target("http://localhost:8080/mtango/rest/device/sys/tg_test/1/long_scalar_w")
                .request().get(String.class);
        Response result = gson.fromJson(json, Response.class);
        //gson deserializes any number to double if otherwise is not specified
        assertTrue((int) (double) result.argout == 123456);
    }

    @Test
    public void testGetCommand() throws Exception {
        ResteasyClient cr = new ResteasyClientBuilder().httpEngine(engine).build();
        String json = cr.target("http://localhost:8080/mtango/rest/device/sys/tg_test/1/DevString=Hello World!!!")
                .request().get(String.class);
        Response result = gson.fromJson(json, Response.class);
        assertTrue(result.argout.equals("Hello World!!!"));
    }
}
