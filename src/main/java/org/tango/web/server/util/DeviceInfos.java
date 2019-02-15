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

import org.tango.rest.rc4.entities.DeviceInfo;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 17.12.2015
 */
public class DeviceInfos {
    private DeviceInfos(){}


    public static DeviceInfo fromDeviceInfo(fr.esrf.TangoApi.DeviceInfo deviceInfo){
        DeviceInfo instance = new DeviceInfo();

        instance.last_exported = deviceInfo.last_exported;
        instance.last_unexported = deviceInfo.last_unexported;
        instance.name = deviceInfo.name;
        instance.ior = deviceInfo.ior;
        instance.version = deviceInfo.version;
        instance.exported = deviceInfo.exported;
        instance.pid = deviceInfo.pid;
        instance.server = deviceInfo.server;
        instance.hostname = deviceInfo.hostname;
        instance.classname = deviceInfo.classname;
        instance.is_taco = deviceInfo.is_taco;

        return instance;
    }
}
