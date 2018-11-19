package org.tango.web.server.tree;

import org.tango.web.server.proxy.TangoDatabaseProxy;

import java.util.List;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11/14/18
 */
public class DevicesTreeContext {
    public List<TangoDatabaseProxy> dbs;
    public DeviceFilters filters;

    public DevicesTreeContext(List<TangoDatabaseProxy> dbs, DeviceFilters filters) {
        this.dbs = dbs;
        this.filters = filters;
    }
}
