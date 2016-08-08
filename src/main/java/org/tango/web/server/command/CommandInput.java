package org.tango.web.server.command;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 8/8/16
 */
public class CommandInput {
    public String cmdName;
    public Class<?> type;
    public Object input;

    public CommandInput(String cmdName, Class<?> type, Object converted) {
        this.cmdName = cmdName;
        this.type = type;
        this.input = converted;
    }
}
