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

package org.tango.web.server.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author ingvord
 * @since 5/25/14@1:54 AM
 */
public class TimeWatcher implements Filter {
    private final Logger logger = LoggerFactory.getLogger(TimeWatcher.class);

    public void destroy() {
    }

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws ServletException, IOException {
        //TODO is there any runtime context listeners?
        if ("no".equals(req.getServletContext().getAttribute("mtango.watch.time"))) {
            chain.doFilter(req, resp);
            return;
        }
        logger.info("Serving request. Stopwatch is active.");
        long start = System.nanoTime();
        chain.doFilter(req, resp);
        long end = System.nanoTime();
        long delta = end - start;
        long delta_ms = TimeUnit.MILLISECONDS.convert(delta, TimeUnit.NANOSECONDS);
        logger.info("Request processing time (nano):" + delta);
        logger.info("Request processing time (ms):" + delta_ms);
    }

    public void init(FilterConfig config) throws ServletException {

    }

}
