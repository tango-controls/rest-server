package org.tango.web.server.rest;

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
    private Responses() {
    }

    public static void sendSuccess(Object argout, Writer out) {
        Response resp = createSuccessResult(argout);
        Json.GSON.toJson(resp, out);
    }

    public static void sendFailure(Throwable error, Writer out) {
        Response resp = Responses.createFailureResult(createExceptionMessage(error));
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


    public static Response createAttributeSuccessResult(Triplet<Object, Long, Quality> triplet) {
        return new Response(triplet.getValue0(), null, triplet.getValue2(), triplet.getValue1());
    }

    public static Response createSuccessResult(Object argout) {
        return new Response(argout, null, null, System.currentTimeMillis());
    }

    public static Response createFailureResult(String[] messages) {
        if (messages == null || messages.length == 0)
            messages = new String[]{"Unexpected server side error! See server log for details."};
        return new Response(null, messages, Quality.INVALID, System.currentTimeMillis());
    }
}
