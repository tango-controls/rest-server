package hzg.wpn.mtango;

/**
 * Creates CacheableCommandExecutionStrategy with fixed remove delay or with default one {@link CacheableCommandExecutionStrategy.REMOVE_TASK_DEFAULT_DELAY}
 *
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 29.08.13
 */
public class CommandExecutionStrategyFactory {
    CommandExecutionStrategy createCommandExecutionStrategy(String... args) {
        long delay = args.length > 0 ? Long.parseLong(args[0]) : CacheableCommandExecutionStrategy.REMOVE_TASK_DEFAULT_DELAY;
        return new CacheableCommandExecutionStrategy(delay);
    }
}
