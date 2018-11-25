package org.tango.web.server;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.tango.web.server.proxy.*;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author ingvord
 * @since 11/25/18
 */
public class Context {
    public final LoadingCache<String, Optional<TangoDatabaseProxy>> hosts = CacheBuilder.newBuilder()
            .expireAfterAccess(30L, TimeUnit.MINUTES)
            //TODO maximumSize or Weight
            .recordStats()
            .build(CacheLoader.from(Proxies::optionalTangoDatabaseProxy));
    public final LoadingCache<String, Optional<TangoDeviceProxy>> devices = CacheBuilder.newBuilder()
            .expireAfterAccess(30L, TimeUnit.MINUTES)
            //TODO maximumSize or Weight
            .recordStats()
            .build(CacheLoader.from(Proxies::optionalTangoDeviceProxy));
    public final LoadingCache<String, Optional<TangoAttributeProxy>> attributes = CacheBuilder.newBuilder()
            .expireAfterAccess(30L, TimeUnit.MINUTES)
            //TODO maximumSize or Weight
            .recordStats()
            .build(CacheLoader.from(Proxies::optionalTangoAttributeProxy));
    public final LoadingCache<String, Optional<TangoCommandProxy>> commands = CacheBuilder.newBuilder()
            .expireAfterAccess(30L, TimeUnit.MINUTES)
            //TODO maximumSize or Weight
            .recordStats()
            .build(CacheLoader.from(Proxies::optionalTangoCommandProxy));
}
