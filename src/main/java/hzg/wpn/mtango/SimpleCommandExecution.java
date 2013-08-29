package hzg.wpn.mtango;

import hzg.wpn.mtango.command.Command;
import hzg.wpn.mtango.command.CommandExecutionException;

/**
 * This strategy just invokes {@link hzg.wpn.mtango.command.Command#execute()}
 *
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 29.08.13
 */
public class SimpleCommandExecution implements CommandExecutionStrategy {
    @Override
    public Object execute(Command cmd) throws CommandExecutionException {
        return cmd.execute();
    }
}
