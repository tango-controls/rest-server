package org.tango.web.server.rest;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 26.06.14
 */
public class DeviceState {
    public String state;
    public String status;

    public DeviceState(String state, String status) {
        this.state = state;
        this.status = status;
    }
}
