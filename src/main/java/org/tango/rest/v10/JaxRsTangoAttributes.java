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

package org.tango.rest.v10;

import org.javatuples.Pair;
import org.tango.rest.v10.entities.Attribute;
import org.tango.rest.v10.entities.AttributeValue;
import org.tango.web.server.TangoProxiesCache;
import org.tango.web.server.binding.DynamicValue;
import org.tango.web.server.binding.Partitionable;
import org.tango.web.server.binding.RequiresTangoSelector;
import org.tango.web.server.binding.StaticValue;
import org.tango.web.server.proxy.TangoAttributeProxy;
import org.tango.web.server.util.TangoRestEntityUtils;
import org.tango.web.server.util.TangoSelector;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11/16/18
 */
@Produces(MediaType.APPLICATION_JSON)
public class JaxRsTangoAttributes {

    @GET
    @Partitionable
    @StaticValue
    @RequiresTangoSelector
    public List<Attribute> get(@Context TangoSelector tangoSelector, final @Context UriInfo uriInfo){
        return tangoSelector.selectAttributesStream()
                .map(tangoAttribute -> TangoRestEntityUtils.fromTangoAttribute(tangoAttribute, uriInfo))
                .collect(Collectors.toList());
    }

    @GET
    @Partitionable
    @DynamicValue
    @RequiresTangoSelector
    @Path("/value")
    public List<Object> read(@Context TangoSelector tangoSelector, final @Context UriInfo uriInfo){
        return tangoSelector.selectAttributesStream()
                .map(tangoAttribute -> TangoRestEntityUtils.fromTangoAttribute(tangoAttribute, uriInfo))
                .map(TangoRestEntityUtils::getValueFromTangoAttribute)
                .collect(Collectors.toList());
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Partitionable
    @DynamicValue
    @Path("/value")
    public List<AttributeValue<?>> write(@DefaultValue("false") @QueryParam("async") boolean async, @Context TangoProxiesCache proxies, List<AttributeValue<?>> values) {
        Stream<Pair<AttributeValue<?>, TangoAttributeProxy>> pairs = values.stream()
                .map(attributeValue -> new Pair<>(attributeValue, proxies.attributes.getUnchecked(String.format("tango://%s/%s/%s", attributeValue.host, attributeValue.device, attributeValue.name))))
                .filter(pair -> pair.getValue1().isPresent())
                .map(pair -> new Pair<>(pair.getValue0(), pair.getValue1().get()));

        if (async) {
            //TODO servlet async
            CompletableFuture.runAsync(() -> pairs.forEach(TangoRestEntityUtils::setValueToTangoAttribute));
            return null;
        } else {
            return pairs.map(TangoRestEntityUtils::setValueToTangoAttribute).collect(Collectors.toList());
        }
    }

}
