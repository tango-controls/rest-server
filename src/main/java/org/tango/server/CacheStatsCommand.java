package org.tango.server;

import com.google.common.cache.CacheStats;
import fr.esrf.Tango.DevVarDoubleStringArray;

/**
 * @author ingvord
 * @since 11/25/18
 */
public class CacheStatsCommand {
    private CacheStats cacheStats;

    public CacheStatsCommand(CacheStats cacheStats) {
        this.cacheStats = cacheStats;
    }

    public DevVarDoubleStringArray toDevVarDoubleStringArray(){
        return new DevVarDoubleStringArray(
                new double[]{
                        cacheStats.hitCount(),
                        cacheStats.missCount(),
                        cacheStats.loadCount(),
                        cacheStats.loadSuccessCount(),
                        cacheStats.loadExceptionCount(),
                        cacheStats.totalLoadTime(),
                        cacheStats.hitRate(),
                        cacheStats.missRate(),
                        cacheStats.averageLoadPenalty(),
                        cacheStats.loadExceptionRate()
                },
                new String[]{
                        "hitCount",
                        "missCount",
                        "loadCount",
                        "loadSuccessCount",
                        "loadExceptionCount",
                        "totalLoadTime",
                        "hitRate",
                        "missRate",
                        "averageLoadPenalty",
                        "loadExceptionRate"
                }
        );
    }
}
