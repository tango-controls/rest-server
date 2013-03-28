package hzg.wpn.mtango;

import wpn.hdri.tango.proxy.TangoProxyException;
import wpn.hdri.tango.proxy.TangoProxyWrapper;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 10.10.12
 */
public class TangoProxyWrapperFactory {
    public TangoProxyWrapper createTangoProxyWrapper(String tangoUrl) throws TangoProxyException {
        return new TangoProxyWrapper(tangoUrl);
    }
}
