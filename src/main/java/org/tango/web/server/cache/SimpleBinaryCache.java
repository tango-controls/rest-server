package org.tango.web.server.cache;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;

import java.util.concurrent.ConcurrentMap;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 4/21/17
 */
public class SimpleBinaryCache {
    private final ConcurrentMap<String, CachedEntity> cache;

    public SimpleBinaryCache(int capacity) {
        this.cache = new ConcurrentLinkedHashMap.Builder<String, CachedEntity>()
                .maximumWeightedCapacity(capacity)
                .build();
    }

    public void add(CachedEntity entity) {
        cache.put(entity.uri, entity);
    }

    public CachedEntity get(String uri) {
        return cache.get(uri);
    }
}
