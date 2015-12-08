package org.tango.web.server;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.gson.Gson;
import com.sun.org.apache.xpath.internal.operations.*;
import fr.esrf.Tango.DevError;
import fr.esrf.Tango.DevFailed;
import org.tango.web.rest.Response;

import java.io.Writer;
import java.lang.String;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author ingvord
 * @since 5/24/14@8:10 PM
 */
public class Responses {
    private final static Gson GSON = new Gson();

    private Responses() {
    }

    public static void sendSuccess(Object argout, Writer out) {
        Response resp = createSuccessResult(argout);
        GSON.toJson(resp, out);
    }

    public static void sendFailure(Throwable error, Writer out) {
        Response resp = Responses.createFailureResult(error);
        GSON.toJson(resp, out);
    }

    public static void sendFailure(DevFailed devFailed, Writer out) {
        Response resp = Responses.createFailureResult(devFailed);
        GSON.toJson(resp, out);
    }


    private static String[] createExceptionMessage(Throwable e) {
        ArrayList<String> result = new ArrayList<String>();
        do {
            if (e.getLocalizedMessage() != null)
                result.add(e.getLocalizedMessage());
        }
        while ((e = e.getCause()) != null);
        return result.toArray(new String[result.size()]);
    }

    public static <T> Response<T> createAttributeSuccessResult(T value, long timestamp, String quality) {
        return new Response<T>(value, null, quality, timestamp);
    }

    public static <T> Response<T> createSuccessResult(T argout) {
        return createSuccessResult(argout, System.currentTimeMillis());
    }

    public static <T> Response<T> createSuccessResult(T argout, long timestamp) {
        return createAttributeSuccessResult(argout, timestamp, null);
    }

    public static <T> Response<T> createFailureResult(Throwable cause) {
        return new Response<>(null, createExceptionMessage(cause), "INVALID", System.currentTimeMillis());
    }

    public static <T> Response<T> createFailureResult(String message) {
        return new Response<>(null, new String[]{message}, "INVALID", System.currentTimeMillis());
    }

    public static <T> Response<T> createFailureResult(String message, Throwable cause) {
        return new Response<>(null, createExceptionMessage(new Exception(message, cause)), "INVALID", System.currentTimeMillis());
    }

    public static <T> Response<T> createFailureResult(DevFailed devFailed) {
        return new Response<>(null, Iterables.toArray(Iterables.transform(
                Arrays.asList(devFailed.errors), new Function<DevError, String>() {
                    @Override
                    public String apply(DevError input) {
                        return String.format("%s: %s - %s", input.severity, input.reason, input.desc);
                    }
                }), String.class), "INVALID", System.currentTimeMillis());
    }

    public static <T> Response<T> createFailureResult(String msg, DevFailed devFailed) {
        return new Response<>(null, Iterables.toArray(Iterables.concat(
                Arrays.asList(msg), Iterables.transform(Arrays.asList(devFailed.errors), new Function<DevError, String>() {
                    @Override
                    public String apply(DevError input) {
                        return String.format("%s: %s - %s", input.severity, input.reason, input.desc);
                    }
                })), String.class), "INVALID", System.currentTimeMillis());
    }
}
