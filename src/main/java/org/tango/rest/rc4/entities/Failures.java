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

package org.tango.rest.rc4.entities;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import fr.esrf.Tango.DevError;
import fr.esrf.Tango.DevFailed;
import org.tango.client.ez.proxy.TangoProxyException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 4/18/17
 */
public class Failures {
    private Failures() {
    }

    public static Failure createInstance(String msg) {
        return new Failure(
                new Failure.Error[]{
                        new Failure.Error(
                                msg, "", "", Thread.currentThread().getStackTrace()[2].toString())},
                System.currentTimeMillis());
    }

    public static Failure createInstance(TangoProxyException cause) {
        return new Failure(
                new Failure.Error[]{
                        new Failure.Error(
                                cause.device, cause.reason, cause.severity, cause.origin)},
                System.currentTimeMillis());
    }

    public static Failure createInstance(Throwable cause) {
        return new Failure(throwableToErrors(cause), System.currentTimeMillis());
    }

    public static Failure createInstance(DevFailed devFailed) {
        return new Failure(Lists.transform(
                Arrays.asList(devFailed.errors), new Function<DevError, Failure.Error>() {
                    @Override
                    public Failure.Error apply(DevError input) {
                        return new Failure.Error(input.reason, input.desc, input.severity.toString(), input.origin);
                    }
                }).toArray(new Failure.Error[devFailed.errors.length]), System.currentTimeMillis());
    }

    private static Failure.Error[] throwableToErrors(Throwable throwable) {
        List<Failure.Error> result = new ArrayList<>();
        do {
            result.add(new Failure.Error(
                    throwable.getClass().getSimpleName(), throwable.getMessage(), "ERR", throwable.getStackTrace()[0].toString()));
        } while ((throwable = throwable.getCause()) != null);
        return result.toArray(new Failure.Error[result.size()]);
    }
}
