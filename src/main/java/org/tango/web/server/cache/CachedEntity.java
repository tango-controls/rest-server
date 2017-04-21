package org.tango.web.server.cache;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 4/21/17
 */
public class CachedEntity {
    public final String uri;
    public final byte[] value;
    public final long timestamp;

    public CachedEntity(String uri, long timestamp, byte[] value) {
        this.uri = uri;
        this.value = value;
        this.timestamp = timestamp;
    }
}
