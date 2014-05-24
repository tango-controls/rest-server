package hzg.wpn.mtango.server;

import hzg.wpn.mtango.util.Json;
import hzg.wpn.tango.client.attribute.Quality;
import org.javatuples.Triplet;

import java.io.Writer;

/**
 * @author ingvord
 * @since 5/24/14@8:10 PM
 */
public class TangoProxyResponse {
    private final Object argout;
    private final String[] error;
    private final Quality quality;
    private final long timestamp;

    private TangoProxyResponse(Object argout, String[] error, Quality quality, long timestamp) {
        this.argout = argout;
        this.error = error;
        this.quality = quality;
        this.timestamp = timestamp;
    }

    public static void sendSuccess(Object argout, Writer out) {
        TangoProxyResponse resp = createSuccessResult(argout);
        Json.GSON.toJson(resp, out);
    }

    public static void sendFailure(String[] errors, Writer out) {
        TangoProxyResponse resp = TangoProxyResponse.createFailureResult(errors);
        Json.GSON.toJson(resp, out);
    }

    public static TangoProxyResponse createSuccessResult(Object argout) {
        if (argout != null && Triplet.class.isAssignableFrom(argout.getClass())) {
            Triplet<Object, Long, Quality> triplet = (Triplet<Object, Long, Quality>) argout;
            return new TangoProxyResponse(triplet.getValue0(), null, triplet.getValue2(), triplet.getValue1());
        } else {
            return new TangoProxyResponse(argout, null, Quality.VALID, System.currentTimeMillis());
        }
    }

    public static TangoProxyResponse createFailureResult(String[] message) {
        if (message == null || message.length == 0)
            message = new String[]{"Unexpected server side error! See server log for details."};
        return new TangoProxyResponse(null, message, Quality.INVALID, System.currentTimeMillis());
    }
}
