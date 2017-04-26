package org.tango.web.server.attribute;

import fr.esrf.TangoApi.DbDatum;

/**
 * Facade class for {@link DbDatum}
 *
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 4/26/17
 */
public class AttributeProperty {
    public String name;
    public String[] values;

    public AttributeProperty(DbDatum dbDatum) {
        this.name = dbDatum.name;
        this.values = dbDatum.extractStringArray();
    }
}
