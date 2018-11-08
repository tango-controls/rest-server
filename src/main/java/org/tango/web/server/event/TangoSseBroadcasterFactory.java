package org.tango.web.server.event;

import javax.ws.rs.sse.Sse;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11/8/18
 */
public class TangoSseBroadcasterFactory {


    private final Sse sse;

    public TangoSseBroadcasterFactory(Sse sse) {
        this.sse = sse;
    }

    public TangoSseBroadcaster newInstance() throws Exception {
        return new TangoSseBroadcaster(sse.newBroadcaster());
    }

    public Sse getSse() {
        return sse;
    }
}
