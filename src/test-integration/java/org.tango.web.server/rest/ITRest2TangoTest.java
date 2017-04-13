package org.tango.web.server.rest;

import org.junit.Test;
import org.tango.rest.ClientHelper;
import org.tango.rest.entities.AttributeInfo;
import org.tango.rest.entities.CommandInfo;
import org.tango.rest.entities.DeviceInfo;
import org.tango.rest.entities.DeviceState;
import org.tango.rest.response.IntegerResponse;
import org.tango.rest.response.ListStringResponse;
import org.tango.rest.response.Response;
import org.tango.rest.response.StringResponse;

import javax.ws.rs.client.Client;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Ingvord
 * @since 05.07.14
 */
public class ITRest2TangoTest {
    private final Client client = ClientHelper.initializeClientWithBasicAuthentication("localhost:8080", "ingvord", "test");

    @Test
    public void testGetDevices() throws Exception {
        Response<List<String>> result = client.target("http://localhost:8080/mtango/rest/devices")
                .request().get(new ListStringResponse());
        System.out.println(result);
        assertTrue(result.argout.contains("sys/tg_test/1"));
    }

    @Test
    public void testGetDevice() throws Exception {
        DeviceState result = client.target("http://localhost:8080/mtango/rest/device/sys/tg_test/1")
                .request().get(DeviceState.class);
        assertTrue(result.state.equals("RUNNING"));
        assertTrue(result.status.equals("The device is in RUNNING state."));
    }

    @Test
    public void testGetDeviceInfo() throws Exception {
        DeviceInfo result = client.target("http://localhost:8080/mtango/rest/device/sys/tg_test/1/info")
                .request().get(DeviceInfo.class);
        assertTrue(result.name.equals("sys/tg_test/1"));
    }

    @Test
    public void testGetDeviceAttributes() throws Exception {
        Response<List<String>> result = client.target("http://localhost:8080/mtango/rest/device/sys/tg_test/1/attributes")
                .request().get(new ListStringResponse());
        assertTrue(result.argout.contains("long_scalar_w"));
    }

    @Test
    public void testGetDeviceCommands() throws Exception {
        Response<List<String>> result = client.target("http://localhost:8080/mtango/rest/device/sys/tg_test/1/commands")
                .request().get(new ListStringResponse());
        assertTrue(result.argout.contains("DevString"));
    }

    @Test
    public void testPutAttribute() throws Exception {
        client.target("http://localhost:8080/mtango/rest/device/sys/tg_test/1/long_scalar_w=1234567")
                .request().put(null);
//        System.out.println(result);
    }

    @Test
    public void testGetAttributeInfo() throws Exception {
        AttributeInfo result = client.target("http://localhost:8080/mtango/rest/device/sys/tg_test/1/long_scalar_w/info")
                .request().get(AttributeInfo.class);
        assertTrue(result.name.equals("long_scalar_w"));
    }

    @Test
    public void testGetCommandInfo() throws Exception {
        CommandInfo result = client.target("http://localhost:8080/mtango/rest/device/sys/tg_test/1/DevString/info")
                .request().get(CommandInfo.class);
        assertTrue(result.cmd_name.equals("DevString"));
    }

    @Test
    public void testGetAttribute() throws Exception {
        Response<Integer> result = client.target("http://localhost:8080/mtango/rest/device/sys/tg_test/1/long_scalar_w")
                .request().get(new IntegerResponse());
        assertTrue(result.argout == 1234567);//was written in the previous test
    }

    @Test
    public void testGetCommand() throws Exception {
        Response<String> result = client.target("http://localhost:8080/mtango/rest/device/sys/tg_test/1/DevString=Hello World!!!")
                .request().get(new StringResponse());
        assertTrue(result.argout.equals("Hello World!!!"));
    }

    @Test
    public void testGeneralPurposeResponse() throws Exception {
        Response result = client.target("http://localhost:8080/mtango/rest/device/sys/tg_test/1/DevVarLongArray=1,2,3,4,5")
                .request().get(Response.class);
        assertArrayEquals(new Double[]{1., 2., 3., 4., 5.}, ((List<Double>) result.argout).toArray(new Double[5]));
    }
}
