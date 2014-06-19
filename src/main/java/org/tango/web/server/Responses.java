package org.tango.web.server;

import hzg.wpn.tango.client.attribute.Quality;
import org.javatuples.Triplet;
import org.tango.web.server.util.Json;

import java.io.Writer;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author ingvord
 * @since 5/24/14@8:10 PM
 */
public class Responses {
    private final Object argout;
    private final String[] error;
    private final Quality quality;
    private final long timestamp;

    Responses(Object argout, String[] error, Quality quality, long timestamp) {
        this.argout = argout;
        this.error = error;
        this.quality = quality;
        this.timestamp = timestamp;
    }

    public static void sendSuccess(Object argout, Writer out) {
        Responses resp = createSuccessResult(argout);
        Json.GSON.toJson(resp, out);
    }

    public static void sendFailure(Throwable error, Writer out) {
        Responses resp = Responses.createFailureResult(createExceptionMessage(error));
        Json.GSON.toJson(resp, out);
    }

    private static String[] createExceptionMessage(Throwable e) {
        Set<String> result = new LinkedHashSet<String>();
        do {
            if (e.getMessage() != null)
                result.add(e.getMessage());
        }
        while ((e = e.getCause()) != null);
        return result.toArray(new String[result.size()]);
    }


    public static Responses createSuccessResult(Object argout) {
        if (argout != null && Triplet.class.isAssignableFrom(argout.getClass())) {
            Triplet<Object, Long, Quality> triplet = (Triplet<Object, Long, Quality>) argout;
            return new Responses(triplet.getValue0(), null, triplet.getValue2(), triplet.getValue1());
        } else {
            return new Responses(argout, null, Quality.VALID, System.currentTimeMillis());
        }
    }

    public static Responses createFailureResult(String[] message) {
        if (message == null || message.length == 0)
            message = new String[]{"Unexpected server side error! See server log for details."};
        return new Responses(null, message, Quality.INVALID, System.currentTimeMillis());
    }
}
