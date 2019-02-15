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

package org.tango.web.server.proxy;

import fr.esrf.Tango.AttrQuality;
import fr.esrf.Tango.AttrWriteType;
import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.AttributeProxy;
import fr.esrf.TangoApi.DeviceAttribute;
import fr.soleil.tango.clientapi.TangoAttribute;

/**
 * @author ingvord
 * @since 11/18/18
 */
public interface TangoAttributeProxy {
    void write() throws DevFailed;

    void write(Object value) throws DevFailed;

    <T> void writeImage(int dimX, int dimY, Object values) throws DevFailed;

    void update() throws DevFailed;

    <T> T read(Class<T> type) throws DevFailed;

    Object read() throws DevFailed;

    Number readAsNumber() throws DevFailed;

    <T> Object readArray(Class<T> type) throws DevFailed;

    <T> Object readWrittenArray(Class<T> type) throws DevFailed;

    <T> T readWritten(Class<T> type) throws DevFailed;

    Object readWritten() throws DevFailed;

    Number readWrittenAsNumber() throws DevFailed;

    <T> T[] readSpecOrImage(Class<T> type) throws DevFailed;

    Number[] readSpecOrImageAsNumber() throws DevFailed;

    <T> T[] readWrittenSpecOrImage(Class<T> type) throws DevFailed;

    Number[] readWrittenSpecOrImageAsNumber() throws DevFailed;

    String readAsString(String separator, String endSeparator) throws DevFailed;

    void insert(Object value) throws DevFailed;

    void insertImage(int dimX, int dimY, Object values) throws DevFailed;

    <T> T extract(Class<T> type) throws DevFailed;

    <T> Object extractArray(Class<T> type) throws DevFailed;

    <T> Object extractWrittenArray(Class<T> type) throws DevFailed;

    Number extractNumber() throws DevFailed;

    Object extract() throws DevFailed;

    Object extractWritten() throws DevFailed;

    <T> T extractWritten(Class<T> type) throws DevFailed;

    Number extractNumberWritten() throws DevFailed;

    <T> T[] extractSpecOrImage(Class<T> type) throws DevFailed;

    Number[] extractNumberSpecOrImage() throws DevFailed;

    <T> T[] extractWrittenSpecOrImage(Class<T> type) throws DevFailed;

    Number[] extractNumberWrittenSpecOrImage() throws DevFailed;

    String extractToString(String separator, String endSeparator) throws DevFailed;

    boolean isNumber();

    boolean isBoolean();

    boolean isString();

    boolean isWritable();

    boolean isScalar();

    boolean isSpectrum();

    boolean isImage();

    AttributeProxy getAttributeProxy();

    DeviceAttribute getDeviceAttribute();

    int getDimX() throws DevFailed;

    int getDimY() throws DevFailed;

    int getWrittenDimX() throws DevFailed;

    int getWrittenDimY() throws DevFailed;

    int getDataType() throws DevFailed;

    AttrWriteType getWriteType();

    long getTimestamp() throws DevFailed;

    AttrQuality getQuality() throws DevFailed;

    String getDeviceName() throws DevFailed;

    String getName();

    void setTimeout(int timeout) throws DevFailed;

    TangoAttribute asTangoAttribute();
}
