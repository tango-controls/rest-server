package hzg.wpn.mtango.command;

import hzg.wpn.tango.client.proxy.TangoProxy;

import java.lang.reflect.Method;
import java.util.concurrent.*;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11.10.12
 */
public class Commands {
    public static Command createCommand(CommandInfo info, TangoProxy proxy) {
        CommandType type = CommandType.valueOf(info.type.toUpperCase());
        switch (type) {
            case READ:
                return createReadCommand(info, proxy);
            case WRITE:
                return createWriteCommand(info, proxy);
            case EXECUTE:
                return createExecCommand(info, proxy);
            default:
                throw new IllegalArgumentException("Unknown action type[" + type + "]");
        }
    }

    public static Command createReadCommand(CommandInfo info, TangoProxy proxy) {
        try {
            Method method = proxy.getClass().getMethod("readAttributeValueTimeQuality", String.class);
            String attributeName = info.target;

            return new Command(proxy, method, attributeName);
        } catch (NoSuchMethodException e) {
            throw new AssertionError(e);
        }
    }

    public static Command createWriteCommand(CommandInfo info, TangoProxy proxy) {
        try {
            Method method = proxy.getClass().getMethod("writeAttribute", String.class, Object.class);
            String attributeName = info.target;
            Object arg = info.convertArgin(proxy.getAttributeInfo(attributeName).getType().getDataType());

            return new Command(proxy, method, attributeName, arg);
        } catch (NoSuchMethodException e) {
            throw new AssertionError(e);
        }
    }

    public static Command createExecCommand(CommandInfo info, TangoProxy proxy) {
        try {
            Method method = proxy.getClass().getMethod("executeCommand", String.class, Object.class);
            String cmdName = info.target;
            Object arg = info.convertArgin(proxy.getCommandInfo(cmdName).getArginType());

            return new Command(proxy, method, cmdName, arg);
        } catch (NoSuchMethodException e) {
            throw new AssertionError(e);
        }
    }

    public static final long REMOVE_TASK_DEFAULT_DELAY = 200;
    private static final ScheduledExecutorService EXEC = Executors.newSingleThreadScheduledExecutor();


    private static final ConcurrentMap<Command, FutureTask<Object>> currentTasks = new ConcurrentHashMap<Command, FutureTask<Object>>();

    public static Object execute(final Command cmd) throws CommandExecutionException {
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
            }, REMOVE_TASK_DEFAULT_DELAY, TimeUnit.MILLISECONDS);
        }
    }
}
