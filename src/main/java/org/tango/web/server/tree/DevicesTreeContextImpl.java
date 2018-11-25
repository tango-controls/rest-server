package org.tango.web.server.tree;

import org.tango.web.server.proxy.TangoDatabaseProxy;

import java.util.List;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11/14/18
 */
public class DevicesTreeContextImpl implements DevicesTreeContext {
    private List<TangoDatabaseProxy> dbs;
    private DeviceFilters filters;

    public DevicesTreeContextImpl(List<TangoDatabaseProxy> dbs, DeviceFilters filters) {
        this.dbs = dbs;
        this.filters = filters;
    }


    public List<TangoDatabaseProxy> getHosts() {
        return dbs;
    }

    public DeviceFilters getWildcards() {
        return filters;
    }
}
