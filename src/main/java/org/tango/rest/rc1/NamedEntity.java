package org.tango.rest.rc1;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 27.11.2015
 */
public class NamedEntity {
    public final String name;
    public final String href;

    public NamedEntity(String name, String href) {
        this.name = name;
        this.href = href;
    }
}
