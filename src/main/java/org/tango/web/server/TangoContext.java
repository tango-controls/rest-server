package org.tango.web.server;

import java.util.concurrent.TimeUnit;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 14.12.2015
 */
public class TangoContext {
    public static final String TANGO_CONTEXT = "org.tango.rest.server.context";

    public volatile long tangoProxyKeepAliveDelay = 30L;
    public volatile TimeUnit tangoProxyKeepAliveDelayTimeUnit = TimeUnit.MINUTES;

    public volatile DatabaseDs databaseDs;
    public volatile DeviceMapper deviceMapper;
    public volatile AccessControl accessControl;
}
