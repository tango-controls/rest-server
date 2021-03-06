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

package org.tango.web.server.tree;

import com.google.common.collect.Lists;
import fr.esrf.Tango.DevFailed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tango.rest.v10.tree.TangoAlias;
import org.tango.utils.DevFailedUtils;
import org.tango.web.server.proxy.TangoDatabaseProxy;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 9/8/18
 */
public class DeviceFilters {
    private final Logger logger = LoggerFactory.getLogger(DeviceFilters.class);

    private List<DeviceFilter> filters;

    public DeviceFilters(List<String> filters) {
        if (filters == null || filters.isEmpty())
            this.filters = Lists.newArrayList(
                    DeviceFilter.valueOf("*/*/*"));
        else this.filters = filters.stream()
                .map(DeviceFilter::valueOf)
                .distinct()
                .collect(Collectors.toList());
    }

    public List<String> getDomains(String host, TangoDatabaseProxy db) {
        return filters.stream().map(filter -> {
            try {
                return db.asEsrfDatabase().get_device_domain(filter.domain  + "*");
            } catch (DevFailed devFailed) {
                logger.warn("Failed to get domain list for {} due to {}", host, DevFailedUtils.toString(devFailed));
                return null;
            }
        }).flatMap(Arrays::stream)
                .distinct()
                .collect(Collectors.toList());
    }

    public List<String> getFamilies(String host, TangoDatabaseProxy db, String domain) {
        return filters.stream()
                .filter(filter -> filter.domain.equals(domain)  || filter.domain.equals("*"))
                .map(filter -> filter.family + "*")
                .distinct()
                .map(family -> {
                    try {
                        return db.asEsrfDatabase().get_device_family(domain + "/" + family);
                    } catch (DevFailed devFailed) {
                        logger.warn("Failed to get family list for {} due to {}", host, DevFailedUtils.toString(devFailed));
                        return null;
                    }
                }).flatMap(Arrays::stream)
                .distinct()
                .collect(Collectors.toList());
    }

    public List<String> getMembers(String host, TangoDatabaseProxy db, String domain, String family) {
        return filters.stream()
                .filter(filter -> filter.domain.equals(domain) || filter.domain.equals("*"))
                .filter(filter -> filter.family.equals(family) || filter.family.equals("*"))
                .map(filter -> filter.member  + "*")
                .distinct()
                .map(member -> {
                    try {
                        return db.asEsrfDatabase().get_device_member(domain + "/" + family + "/" + member);
                    } catch (DevFailed devFailed) {
                        logger.warn("Failed to get member list for {} due to {}", host, DevFailedUtils.toString(devFailed));
                        return null;
                    }
                }).flatMap(Arrays::stream).collect(Collectors.toList());
    }

    public boolean checkDevice(TangoAlias alias) {
        DeviceFilter deviceFilter = DeviceFilter.valueOf(alias.device_name);
        return filters.contains(deviceFilter);
    }

    private static class DeviceFilter {
        public String domain;
        public String family;
        public String member;

        public DeviceFilter(String domain, String family, String member) {
            this.domain = domain;
            this.family = family;
            this.member = member;
        }

        public static DeviceFilter valueOf(String filter){
            String[] parts = filter.split("/");
            return new DeviceFilter(parts[0], parts[1], parts[2]);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DeviceFilter that = (DeviceFilter) o;
            return (Objects.equals(domain, "*") || Objects.equals(that.domain, "*") ||  Objects.equals(domain, that.domain)) &&
                    (Objects.equals(family, "*") || Objects.equals(that.family, "*") || Objects.equals(family, that.family)) &&
                    (Objects.equals(member, "*") || Objects.equals(that.member, "*") || Objects.equals(member, that.member));
        }

        @Override
        public int hashCode() {

            return Objects.hash(domain, family, member);
        }
    }
}
