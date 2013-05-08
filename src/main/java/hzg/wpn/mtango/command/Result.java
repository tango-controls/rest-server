package hzg.wpn.mtango.command;

import org.javatuples.Triplet;
import wpn.hdri.tango.attribute.Quality;

/**
 * Matches to mTango/CommandOut.js
 *
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11.10.12
 */
public class Result {
    private final Object argout;
    private final String[] error;
    private final Quality quality;
    private final long timestamp;

    private Result(Object argout, String[] error, Quality quality, long timestamp) {
        this.argout = argout;
        this.error = error;
        this.quality = quality;
        this.timestamp = timestamp;
    }

    public static Result createSuccessResult(Object argout){
        if (argout != null && Triplet.class.isAssignableFrom(argout.getClass())) {
            Triplet<Object, Long, Quality> triplet = (Triplet<Object, Long, Quality>) argout;
            return new Result(triplet.getValue0(), null, triplet.getValue2(), triplet.getValue1());
        } else {
            return new Result(argout,null,Quality.VALID,System.currentTimeMillis());
        }
    }

    public static Result createFailureResult(String[] message){
        if(message == null || message.length == 0)
            message = new String[]{"Unexpected server side error! See server log for details."};
        return new Result(null, message, Quality.INVALID, System.currentTimeMillis());
    }
}
