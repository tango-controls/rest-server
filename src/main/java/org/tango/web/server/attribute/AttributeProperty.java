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

package org.tango.web.server.attribute;

import fr.esrf.TangoApi.DbDatum;

/**
 * Facade class for {@link DbDatum}
 *
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 4/26/17
 */
public class AttributeProperty {
    public String name;
    public String[] values;

    public AttributeProperty(DbDatum dbDatum) {
        this.name = dbDatum.name;
        this.values = dbDatum.extractStringArray();
    }
}
