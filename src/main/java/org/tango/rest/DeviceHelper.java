package org.tango.rest;

import fr.esrf.TangoApi.CommandInfo;
import fr.esrf.TangoApi.DbDatum;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 8/6/16
 */
public class DeviceHelper {
    public static Object dbDatumToResponse(final DbDatum dbDatum) {
        return new Object() {
            public String name = dbDatum.name;
            public String[] values = dbDatum.extractStringArray();
        };
    }
}
