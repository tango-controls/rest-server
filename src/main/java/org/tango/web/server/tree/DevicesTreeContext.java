package org.tango.web.server.tree;

import org.tango.web.server.proxy.TangoDatabaseProxy;

import java.util.List;

/**
 * @author ingvord
 * @since 11/25/18
 */
public interface DevicesTreeContext {
    List<TangoDatabaseProxy> getHosts();
    DeviceFilters getWildcards();
}
