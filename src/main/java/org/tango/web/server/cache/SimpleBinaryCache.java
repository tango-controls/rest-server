/*
 * Copyright 2019 Tango Controls
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tango.web.server.cache;


import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import org.jboss.resteasy.plugins.cache.server.ServerCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.util.Map;

/**
 * C&Ped from {@link org.jboss.resteasy.plugins.cache.server.SimpleServerCache} to override some behaviour
 */
public class SimpleBinaryCache implements ServerCache {
    private final Logger logger = LoggerFactory.getLogger(SimpleBinaryCache.class);

    private Map<String, CacheEntry> cache;


    public SimpleBinaryCache(int capacity) {
        this.cache = new ConcurrentLinkedHashMap.Builder<String, CacheEntry>()
                .maximumWeightedCapacity(capacity)
                .build();
    }

    @Override
    public Entry get(String uri, MediaType accept) {
        CacheEntry cacheEntry = cache.get(uri);
        logger.debug("got CacheEntry for {}: {}", uri, cacheEntry!=null);
        return cacheEntry;
    }

    @Override
    public Entry add(String uri, MediaType mediaType, CacheControl cc, MultivaluedMap<String, Object> headers, byte[] entity, String etag) {
        CacheEntry cacheEntry = new CacheEntry(headers, entity, cc.getMaxAge(), Long.parseLong(cc.getCacheExtension().get("max-age-millis")), etag);
        cache.put(uri, cacheEntry);
        logger.debug("put CacheEntry for {}", uri);
        return cacheEntry;
    }

    @Override
    public void remove(String key) {
        cache.remove(key);
    }

    @Override
    public void clear() {
        cache.clear();
    }

    public static class CacheEntry implements Entry {
        private final byte[] cached;
        private final int expires;
        private final long maxAgeMillis;
        private final long timestamp = System.currentTimeMillis();
        private final MultivaluedMap<String, Object> headers;
        private String etag;

        private CacheEntry(MultivaluedMap<String, Object> headers, byte[] cached, int expires, long maxAgeMillis, String etag) {
            this.cached = cached;
            this.expires = expires;
            this.headers = headers;
            this.maxAgeMillis = maxAgeMillis;
            this.etag = etag;
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

        public byte[] getCached() {
            return cached;
        }
    }

}
