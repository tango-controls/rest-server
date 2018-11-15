package org.tango.web.server.tree;

import fr.esrf.TangoApi.Database;
import org.tango.web.server.DatabaseDs;

import java.util.List;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11/14/18
 */
public class DevicesTreeContext {
    public List<Database> dbs;
    public DeviceFilters filters;

    public DevicesTreeContext(List<Database> dbs, DeviceFilters filters) {
        this.dbs = dbs;
        this.filters = filters;
    }
}
