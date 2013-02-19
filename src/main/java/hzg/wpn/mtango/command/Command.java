package hzg.wpn.mtango.command;

import wpn.hdri.tango.proxy.TangoProxyWrapper;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11.10.12
 */
public interface Command {
    Object execute() throws CommandExecutionException;
}
