package org.tango.web.server;

import org.javatuples.Triplet;
import org.tango.client.ez.attribute.Quality;
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
    private final String[] errors;
    private final Quality quality;
    private final long timestamp;

    Responses(Object argout, String[] errors, Quality quality, long timestamp) {
        this.argout = argout;
        this.errors = errors;
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


    public static Responses createAttributeSuccessResult(Triplet<Object, Long, Quality> triplet) {
        return new Responses(triplet.getValue0(), null, triplet.getValue2(), triplet.getValue1());
    }

    public static Responses createSuccessResult(Object argout) {
        return new Responses(argout, null, null, System.currentTimeMillis());
    }

    public static Responses createFailureResult(String[] messages) {
        if (messages == null || messages.length == 0)
            messages = new String[]{"Unexpected server side error! See server log for details."};
        return new Responses(null, messages, Quality.INVALID, System.currentTimeMillis());
    }
}
