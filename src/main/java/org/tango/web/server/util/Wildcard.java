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

package org.tango.web.server.util;

import java.util.regex.Pattern;

import static org.tango.web.server.providers.TangoDatabaseProvider.DEFAULT_TANGO_PORT;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11/16/18
 */
public class Wildcard {
    public final Pattern TANGO_HOST_PATTERN = Pattern.compile("\\W+:\\d+");

    public String host;
    public String domain;
    public String family;
    public String member;
    public String attribute;

    public Wildcard() {
    }

    public Wildcard(String host, String domain, String family, String member, String attribute) {
        this.host = host;
        this.domain = domain;
        this.family = family;
        this.member = member;
        this.attribute = attribute;
    }

    public String asDeviceWildcard() {
        //TODO add *???
        return domain + "/" + family + "/" + member;
    }

    public String asFullTangoHost(){
        return TANGO_HOST_PATTERN.matcher(host).matches() ? host : host + ":" + DEFAULT_TANGO_PORT;
    }
}
