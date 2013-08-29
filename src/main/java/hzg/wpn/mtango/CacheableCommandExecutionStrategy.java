package hzg.wpn.mtango;

import hzg.wpn.mtango.command.Command;
import hzg.wpn.mtango.command.CommandExecutionException;

import java.util.concurrent.*;

/**
 * This strategy caches each command result for 200 ms so if subsequent command is similar to the previous one it will
 * return cached result.
 *
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 29.08.13
 */
public class CacheableCommandExecutionStrategy implements CommandExecutionStrategy {
    public static final long REMOVE_TASK_DEFAULT_DELAY = 200;

    private static final ScheduledExecutorService EXEC = Executors.newSingleThreadScheduledExecutor();


    private final ConcurrentMap<Command, FutureTask<Object>> currentTasks = new ConcurrentHashMap<Command, FutureTask<Object>>();


    private final long removeTaskDelay;

    public CacheableCommandExecutionStrategy(long keepDelay) {
        removeTaskDelay = keepDelay;
    }

    @Override
    public Object execute(final Command cmd) throws CommandExecutionException {
        FutureTask<Object> task = currentTasks.get(cmd);
        if (task == null) {
            FutureTask<Object> ft = new FutureTask<Object>(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    return cmd.execute();
                }
            });
            task = currentTasks.putIfAbsent(cmd, ft);
            if (task == null) {
                task = ft;
                task.run();
            }
        }
        try {
            return task.get();
        } catch (Exception e) {
            throw new CommandExecutionException(e);
        } finally {
            EXEC.schedule(new Runnable() {
                @Override
                public void run() {
                    currentTasks.remove(cmd);
                }
            }, removeTaskDelay, TimeUnit.MILLISECONDS);
        }

    }
}
