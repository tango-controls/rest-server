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

import org.tango.web.server.TangoRestContext;
import org.tango.web.server.binding.StaticValue;

import javax.ws.rs.ext.Provider;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 15.12.2015
 */
@Provider
@StaticValue
public class StaticValueCacheControlProvider extends AbstractCacheControlProvider {
    public StaticValueCacheControlProvider(TangoRestContext context) {
        super(context);
    }

    @Override
    protected long getDelay() {
        return context.staticValueExpirationDelay.get();
    }
}
