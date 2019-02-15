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
public class TangoAttributeProxyImpl implements TangoAttributeProxy {
    private final TangoAttribute attribute;

    public TangoAttributeProxyImpl(TangoAttribute attribute) {
        this.attribute = attribute;
    }

    @Override
    public void write() throws DevFailed {
        attribute.write();
    }

    @Override
    public void write(Object value) throws DevFailed {
        attribute.write(value);
    }

    @Override
    public <T> void writeImage(int dimX, int dimY, Object values) throws DevFailed {
        attribute.writeImage(dimX, dimY, values);
    }

    @Override
    public void update() throws DevFailed {
        attribute.update();
    }

    @Override
    public <T> T read(Class<T> type) throws DevFailed {
        return attribute.read(type);
    }

    @Override
    public Object read() throws DevFailed {
        return attribute.read();
    }

    @Override
    public Number readAsNumber() throws DevFailed {
        return attribute.readAsNumber();
    }

    @Override
    public <T> Object readArray(Class<T> type) throws DevFailed {
        return attribute.readArray(type);
    }

    @Override
    public <T> Object readWrittenArray(Class<T> type) throws DevFailed {
        return attribute.readWrittenArray(type);
    }

    @Override
    public <T> T readWritten(Class<T> type) throws DevFailed {
        return attribute.readWritten(type);
    }

    @Override
    public Object readWritten() throws DevFailed {
        return attribute.readWritten();
    }

    @Override
    public Number readWrittenAsNumber() throws DevFailed {
        return attribute.readWrittenAsNumber();
    }

    @Override
    public <T> T[] readSpecOrImage(Class<T> type) throws DevFailed {
        return attribute.readSpecOrImage(type);
    }

    @Override
    public Number[] readSpecOrImageAsNumber() throws DevFailed {
        return attribute.readSpecOrImageAsNumber();
    }

    @Override
    public <T> T[] readWrittenSpecOrImage(Class<T> type) throws DevFailed {
        return attribute.readWrittenSpecOrImage(type);
    }

    @Override
    public Number[] readWrittenSpecOrImageAsNumber() throws DevFailed {
        return attribute.readWrittenSpecOrImageAsNumber();
    }

    @Override
    public String readAsString(String separator, String endSeparator) throws DevFailed {
        return attribute.readAsString(separator, endSeparator);
    }

    @Override
    public void insert(Object value) throws DevFailed {
        attribute.insert(value);
    }

    @Override
    public void insertImage(int dimX, int dimY, Object values) throws DevFailed {
        attribute.insertImage(dimX, dimY, values);
    }

    @Override
    public <T> T extract(Class<T> type) throws DevFailed {
        return attribute.extract(type);
    }

    @Override
    public <T> Object extractArray(Class<T> type) throws DevFailed {
        return attribute.extractArray(type);
    }

    @Override
    public <T> Object extractWrittenArray(Class<T> type) throws DevFailed {
        return attribute.extractWrittenArray(type);
    }

    @Override
    public Number extractNumber() throws DevFailed {
        return attribute.extractNumber();
    }

    @Override
    public Object extract() throws DevFailed {
        return attribute.extract();
    }

    @Override
    public Object extractWritten() throws DevFailed {
        return attribute.extractWritten();
    }

    @Override
    public <T> T extractWritten(Class<T> type) throws DevFailed {
        return attribute.extractWritten(type);
    }

    @Override
    public Number extractNumberWritten() throws DevFailed {
        return attribute.extractNumberWritten();
    }

    @Override
    public <T> T[] extractSpecOrImage(Class<T> type) throws DevFailed {
        return attribute.extractSpecOrImage(type);
    }

    @Override
    public Number[] extractNumberSpecOrImage() throws DevFailed {
        return attribute.extractNumberSpecOrImage();
    }

    @Override
    public <T> T[] extractWrittenSpecOrImage(Class<T> type) throws DevFailed {
        return attribute.extractWrittenSpecOrImage(type);
    }

    @Override
    public Number[] extractNumberWrittenSpecOrImage() throws DevFailed {
        return attribute.extractNumberWrittenSpecOrImage();
    }

    @Override
    public String extractToString(String separator, String endSeparator) throws DevFailed {
        return attribute.extractToString(separator, endSeparator);
    }

    @Override
    public boolean isNumber() {
        return attribute.isNumber();
    }

    @Override
    public boolean isBoolean() {
        return attribute.isBoolean();
    }

    @Override
    public boolean isString() {
        return attribute.isString();
    }

    @Override
    public boolean isWritable() {
        return attribute.isWritable();
    }

    @Override
    public boolean isScalar() {
        return attribute.isScalar();
    }

    @Override
    public boolean isSpectrum() {
        return attribute.isSpectrum();
    }

    @Override
    public boolean isImage() {
        return attribute.isImage();
    }

    @Override
    public AttributeProxy getAttributeProxy() {
        return attribute.getAttributeProxy();
    }

    @Override
    public DeviceAttribute getDeviceAttribute() {
        return attribute.getDeviceAttribute();
    }

    @Override
    public int getDimX() throws DevFailed {
        return attribute.getDimX();
    }

    @Override
    public int getDimY() throws DevFailed {
        return attribute.getDimY();
    }

    @Override
    public int getWrittenDimX() throws DevFailed {
        return attribute.getWrittenDimX();
    }

    @Override
    public int getWrittenDimY() throws DevFailed {
        return attribute.getWrittenDimY();
    }

    @Override
    public int getDataType() throws DevFailed {
        return attribute.getDataType();
    }

    @Override
    public AttrWriteType getWriteType() {
        return attribute.getWriteType();
    }

    @Override
    public long getTimestamp() throws DevFailed {
        return attribute.getTimestamp();
    }

    @Override
    public AttrQuality getQuality() throws DevFailed {
        return attribute.getQuality();
    }

    @Override
    public String getDeviceName() throws DevFailed {
        return attribute.getDeviceName();
    }

    @Override
    public String getName() {
        return attribute.getName();
    }

    @Override
    public void setTimeout(int timeout) throws DevFailed {
        attribute.setTimeout(timeout);
    }

    @Override
    public TangoAttribute asTangoAttribute() {
        return attribute;
    }
}
