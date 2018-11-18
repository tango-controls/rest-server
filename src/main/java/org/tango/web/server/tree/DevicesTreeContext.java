package org.tango.web.server.tree;

import org.tango.web.server.proxy.TangoDatabase;

import java.util.List;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11/14/18
 */
public class DevicesTreeContext {
    public List<TangoDatabase> dbs;
    public DeviceFilters filters;

    public DevicesTreeContext(List<TangoDatabase> dbs, DeviceFilters filters) {
        this.dbs = dbs;
        this.filters = filters;
    }
}
