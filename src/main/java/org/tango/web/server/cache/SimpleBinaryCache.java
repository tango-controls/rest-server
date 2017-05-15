package org.tango.web.server.cache;


import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import org.jboss.resteasy.plugins.cache.server.ServerCache;

import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.util.Map;

/**
 * C&Ped from {@link org.jboss.resteasy.plugins.cache.server.SimpleServerCache} to override some behaviour
 */
public class SimpleBinaryCache implements ServerCache {
    private Map<String, CacheEntry> cache;


    public SimpleBinaryCache(int capacity) {
        this.cache = new ConcurrentLinkedHashMap.Builder<String, CacheEntry>()
                .maximumWeightedCapacity(capacity)
                .build();
    }

    public Entry get(String uri, MediaType accept, MultivaluedMap<String, String> headers) {
        return cache.get(uri);
    }

    public Entry add(String uri, MediaType mediaType, CacheControl cc, MultivaluedMap<String, Object> headers, byte[] entity, String etag, MultivaluedMap<String, String> varyHeaders) {
        CacheEntry cacheEntry = new CacheEntry(headers, entity, cc.getMaxAge(), Long.parseLong(cc.getCacheExtension().get("max-age-millis")), etag, varyHeaders);
        cache.put(uri, cacheEntry);
        return cacheEntry;
    }

    public void remove(String key) {
        cache.remove(key);
    }

    public void clear() {
        cache.clear();
    }

    public static class CacheEntry implements Entry {
        private final byte[] cached;
        private final int expires;
        private final long maxAgeMillis;
        private final long timestamp = System.currentTimeMillis();
        private final MultivaluedMap<String, Object> headers;
        private final MultivaluedMap<String, String> varyHeaders;
        private String etag;

        private CacheEntry(MultivaluedMap<String, Object> headers, byte[] cached, int expires, long maxAgeMillis, String etag, MultivaluedMap<String, String> varyHeaders) {
            this.cached = cached;
            this.expires = expires;
            this.headers = headers;
            this.maxAgeMillis = maxAgeMillis;
            this.etag = etag;
            this.varyHeaders = varyHeaders;
        }

        public int getExpirationInSeconds() {
            return expires - (int) ((System.currentTimeMillis() - timestamp) / 1000);
        }

        public boolean isExpired() {
            return System.currentTimeMillis() - timestamp >= maxAgeMillis;
        }

        public String getEtag() {
            return etag;
        }

        public MultivaluedMap<String, Object> getHeaders() {
            return headers;
        }

        public MultivaluedMap<String, String> getVaryHeaders() {
            return varyHeaders;
        }

        public byte[] getCached() {
            return cached;
        }

    }

}
