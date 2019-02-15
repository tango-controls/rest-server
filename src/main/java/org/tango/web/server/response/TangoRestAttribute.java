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

package org.tango.web.server.response;

import fr.esrf.Tango.DevError;
import fr.esrf.TangoApi.AttributeInfoEx;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.tango.rest.v10.entities.Attribute;
import org.tango.web.server.proxy.TangoAttributeProxy;

import java.net.URI;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11/17/18
 */
public class TangoRestAttribute extends Attribute{
    public AttributeInfoEx info;
    @JsonIgnore
    public TangoAttributeProxy attribute;

    public TangoRestAttribute(String host, String device, String name, AttributeInfoEx info, URI href, TangoAttributeProxy attribute) {
        super(host, device, name, null, href);
        this.info = info;
        this.attribute = attribute;
    }

    public TangoRestAttribute(DevError[] errors) {
        super();
        this.errors = errors;
    }
}
