package hzg.wpn.mtango.command;

import java.util.Map;

/**
 * Matches to mTango/CommandOut.js
 *
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11.10.12
 */
public class Result {
    private final Object argout;
    private final String error;
    private final long timestamp;

    public Result(Object argout, String error) {
        //TODO avoid this if
        if (Map.Entry.class.isAssignableFrom(argout.getClass())) {
            Map.Entry<Object, Long> entry = (Map.Entry<Object, Long>) argout;
            this.argout = entry.getKey();
            this.timestamp = entry.getValue();
        } else {
            this.argout = argout;
            this.timestamp = System.currentTimeMillis();
        }
        this.error = error;
    }

    //TODO ...
}
