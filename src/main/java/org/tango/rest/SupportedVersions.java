package org.tango.rest;

/**
* @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
* @since 27.11.2015
*/
public class SupportedVersions {
    public final String rc1;
    public final String mtango;

    SupportedVersions(String contextRoot) {
        this.rc1 = contextRoot + "/rest/rc1";
        this.mtango = contextRoot + "/rest/mtango";
    }
}
