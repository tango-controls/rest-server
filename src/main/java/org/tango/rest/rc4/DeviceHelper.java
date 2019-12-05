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

package org.tango.rest.rc4;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.CommandInfo;
import fr.esrf.TangoApi.DbDatum;
import fr.esrf.TangoApi.DeviceAttribute;
import fr.esrf.TangoApi.DeviceInfo;
import org.apache.commons.beanutils.ConvertUtils;
import org.tango.client.ez.data.TangoDataWrapper;
import org.tango.client.ez.data.type.TangoDataType;
import org.tango.client.ez.data.type.ValueInsertionException;
import org.tango.client.ez.proxy.NoSuchAttributeException;
import org.tango.client.ez.proxy.TangoProxyException;
import org.tango.client.ez.util.TangoUtils;
import org.tango.web.server.proxy.TangoDeviceProxy;
import org.tango.web.server.util.DeviceInfos;

import java.net.URI;
import java.util.*;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 8/6/16
 */
public class DeviceHelper {
    public static Object deviceToResponse(String devname, final DeviceInfo info,URI href){
        return new org.tango.rest.rc4.entities.Device(devname,
                DeviceInfos.fromDeviceInfo(info),
                href + "/attributes",
                href + "/commands",
                href + "/pipes",
                href + "/properties", href);
    }

    public static Object attributeInfoExToResponse(final String attrName, final String href) {
        return new Object() {
            public String name = attrName;
            public String value = href + "/" + name + "/value";
            public String info = href + "/" + name + "/info";
            public String properties = href + "/" + name + "/properties";
            public String history = href + "/" + name + "/history";
            public Object _links = new Object() {
                public String _self = href;
                //TODO use LinksProvider
            };
        };
    }

    public static Object commandInfoToResponse(final CommandInfo input, final String href) {
        return new Object() {
            public String name = input.cmd_name;
            public CommandInfo info = input;
            public String history = href + "/" + name + "/history";
            public Object _links = new Object() {
                public String _self = href;
            };
        };
    }

    public static Object dbDatumToResponse(final DbDatum dbDatum) {
        return new Object() {
            public String name = dbDatum.name;
            public String[] values = dbDatum.extractStringArray();
        };
    }

    public static fr.esrf.TangoApi.DeviceAttribute[] getDeviceAttributesValue(TangoDeviceProxy deviceProxy, Set<Map.Entry<String, List<String>>> queryParams, boolean async) throws DevFailed {
        //TODO split into good and bad attributes: write good ones; report bad ones (if present)
        fr.esrf.TangoApi.DeviceAttribute[] attrs =
                queryParams.stream()
                        .map(stringListEntry -> {
                            String attrName = stringListEntry.getKey();
                            String[] value = stringListEntry.getValue().toArray(new String[stringListEntry.getValue().size()]);
                            DeviceAttribute result;

                            try {
                                result = new DeviceAttribute(attrName);
                                TangoDataType<Object> dataType = (TangoDataType<Object>) deviceProxy.getProxy().getAttributeInfo(attrName).getType();
                                Class<?> type = dataType.getDataTypeClass();
                                Object converted = ConvertUtils.convert(value.length == 1 ? value[0] : value, type);

                                dataType.insert(TangoDataWrapper.create(result, null), converted);

                                return result;
                            } catch (TangoProxyException | NoSuchAttributeException | ValueInsertionException e) {
                                return null;
                            }
                        })
                        .filter(Objects::nonNull)
                        .toArray(fr.esrf.TangoApi.DeviceAttribute[]::new);
        if(async) {
            deviceProxy.getProxy().toDeviceProxy().write_attribute_asynch(attrs);
            return null;
        } else {
            String[] readNames =
                    Arrays.stream(attrs)
                            .map(deviceAttribute -> {
                                try {
                                    return deviceAttribute.getName();
                                } catch (DevFailed devFailed) {
                                    throw new AssertionError("Must not happen!", TangoUtils.convertDevFailedToException(devFailed));
                                }
                            })
                            .toArray(String[]::new);
            deviceProxy.getProxy().toDeviceProxy().write_attribute(attrs);
            return deviceProxy.getProxy().toDeviceProxy().read_attribute(readNames);
        }
    }
}
