package hzg.wpn.mtango;

import hzg.wpn.mtango.command.Command;
import hzg.wpn.mtango.command.CommandExecutionException;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 29.08.13
 */
public interface CommandExecutionStrategy {
    Object execute(Command cmd) throws CommandExecutionException;
}
