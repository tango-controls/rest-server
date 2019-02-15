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
