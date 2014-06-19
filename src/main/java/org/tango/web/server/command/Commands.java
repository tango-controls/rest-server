package org.tango.web.server.command;

import hzg.wpn.tango.client.proxy.TangoProxy;
import hzg.wpn.tango.client.proxy.TangoProxyException;
import org.tango.web.server.util.Json;

import java.lang.reflect.Method;
import java.util.concurrent.*;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11.10.12
 */
public class Commands {
    private Commands() {
    }

    public static Command createCommand(CommandInfo info, TangoProxy proxy) {
        switch (info.type) {
            case "read":
                return createReadCommand(info, proxy);
            case "write":
                return createWriteCommand(info, proxy);
            case "exec":
                return createExecCommand(info, proxy);
            default:
                throw new IllegalArgumentException("Unknown action type[" + info.type + "]");
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
            Object arg = Json.GSON.fromJson(info.argin, proxy.getAttributeInfo(attributeName).getType().getDataType());

            return new Command(proxy, method, attributeName, arg);
        } catch (NoSuchMethodException | TangoProxyException e) {
            throw new AssertionError(e);
        }
    }

    public static Command createExecCommand(CommandInfo info, TangoProxy proxy) {
        try {
            Method method = proxy.getClass().getMethod("executeCommand", String.class, Object.class);
            String cmdName = info.target;
            Object arg = Json.GSON.fromJson(info.argin, proxy.getCommandInfo(cmdName).getArginType());

            return new Command(proxy, method, cmdName, arg);
        } catch (NoSuchMethodException | TangoProxyException e) {
            throw new AssertionError(e);
        }
    }

    public static final long REMOVE_TASK_DEFAULT_DELAY = 200L;
    private static final ScheduledExecutorService EXEC = Executors.newSingleThreadScheduledExecutor();


    private static final ConcurrentMap<Command, FutureTask<Object>> currentTasks = new ConcurrentHashMap<Command, FutureTask<Object>>();

    public static Object execute(final Command cmd) throws Exception {
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
