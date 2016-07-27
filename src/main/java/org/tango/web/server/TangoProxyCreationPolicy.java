package org.tango.web.server;

import com.google.common.collect.Iterables;
import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevSource;
import fr.esrf.TangoApi.AttributeInfo;
import org.tango.client.ez.proxy.TangoProxy;
import org.tango.client.ez.proxy.TangoProxyException;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 15.12.2015
 */
public class TangoProxyCreationPolicy {
    public volatile DevSource source;
    public final ConcurrentMap<String, AttributeInfo> attributesPolicy = new ConcurrentHashMap<>();

    public TangoProxyCreationPolicy(DevSource source) {
        this.source = source;
    }

    public void apply(TangoProxy proxy) throws TangoProxyException {
        try {
            proxy.toDeviceProxy().set_source(source);

            proxy.toDeviceProxy().set_attribute_info(Iterables.toArray(attributesPolicy.values(), AttributeInfo.class));
        } catch (DevFailed devFailed) {
            throw new TangoProxyException("Failed to apply creation policy for proxy " + proxy.getName(), devFailed);
        }
    }
}
