package org.tango.rest.rc3;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import fr.esrf.Tango.DevFailed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tango.client.ez.proxy.NoSuchCommandException;
import org.tango.client.ez.proxy.TangoProxy;
import org.tango.client.ez.proxy.TangoProxyException;
import org.tango.rest.Device;
import org.tango.rest.SupportedAuthentication;
import org.tango.rest.entities.NamedEntity;
import org.tango.rest.response.Responses;
import org.tango.utils.DevFailedUtils;
import org.tango.web.server.DatabaseDs;
import org.tango.web.server.TangoContext;
import org.tango.web.server.providers.Partitionable;
import org.tango.web.server.providers.StaticValue;

import javax.annotation.Nullable;
import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 8/5/16
 */
@Path("/rc3")
@Produces("application/json")
public class Rc3ApiImpl {
    private final Logger logger = LoggerFactory.getLogger(Rc3ApiImpl.class);

    @Context
    private UriInfo uriInfo;

    @GET
    public Map<String, String> authentication(@Context ServletContext context, @Context TangoContext tangoContext) {
        Map<String, String> result = new HashMap<>();

        result.put("hosts", uriInfo.getAbsolutePath() + "/hosts");
        result.put("x-auth-method", SupportedAuthentication.VALUE);

        return result;
    }

    @GET
    @Path("/hosts")
    public Map<String, String> getHosts(@Context TangoContext tangoContext) throws TangoProxyException {
        Map<String, String> result = Maps.newHashMap();

        for(Map.Entry<String, String> entry : Lists.transform(tangoContext.hostsPool.proxies(), new Function<TangoProxy, Map.Entry<String,String>>() {
            @Override
            public Map.Entry<String,String> apply(@Nullable TangoProxy input) {
                if(input == null) return null;
                try {
                    String tango_host = input.toDeviceProxy().get_tango_host();
                    return new AbstractMap.SimpleEntry<>(tango_host, uriInfo.getAbsolutePath() + tango_host.replace(':', '/'));
                } catch (DevFailed devFailed) {
                    DevFailedUtils.logDevFailed(devFailed, logger);
                    return null;//TODO skip
                }
            }
        }))
            result.put(entry.getKey(), entry.getValue());

        return result;
    }

    @GET
    @Partitionable
    @StaticValue
    @Path("/hosts/{host}/{port}")
    public Object getHost(@Context final DatabaseDs db,
                          @Context final ServletContext context) throws Exception {
        final String[] tangoHost = db.toDeviceProxy().get_tango_host().split(":");
        return new Object(){
            public String name = db.toDeviceProxy().get_name();
            public String host = tangoHost[0];
            public int port = Integer.parseInt(tangoHost[1]);
            public List<String> info = db.getInfo();
            public String devices = uriInfo.getAbsolutePath() + "/devices";
        };
    }

    @GET
    @Path("/hosts/{host}/{port}/devices")
    @StaticValue
    @Partitionable
    public Object get(@QueryParam("wildcard") String wildcard,
                      @Context DatabaseDs db,
                      @Context final ServletContext context){
        try {
            List<String> result = db.getDeviceList(wildcard == null ? "*" : wildcard);
            List<NamedEntity> transform = Lists.transform(result, new Function<String, NamedEntity>() {
                @Override
                public NamedEntity apply(final String input) {
                    return new NamedEntity(input, uriInfo.getAbsolutePath() + "/" + input);
                }
            });
            return transform;
        } catch (NoSuchCommandException | TangoProxyException e) {
            return Responses.createFailureResult(e);
        }
    }

    @Path("/hosts/{host}/{port}/devices/{domain}/{family}/{member}")
    public Device getDevice(){
        return new Device();
    }
}
