package hzg.wpn.mtango.command;

/**
 * Matches to mTango/CommandOut.js
 *
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11.10.12
 */
public class Result {
    private final Object argout;
    private final String error;

    public Result(Object argout, String error) {
        this.argout = argout;
        this.error = error;
    }

    //TODO ...
}
