package hzg.wpn.mtango.command;

import wpn.hdri.tango.proxy.TangoProxyWrapper;

import java.lang.reflect.Method;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 15.10.12
 */
public class CommandImpl implements Command {
    private final TangoProxyWrapper proxy;
    private final Method method;
    private final Object[] args;


    public CommandImpl(TangoProxyWrapper proxy, Method method, Object... args) {
        this.proxy = proxy;
        this.method = method;
        this.args = args;
    }

    public Object execute() throws CommandExecutionException {
        try {
            return method.invoke(proxy,args);
        } catch (Exception e) {
            throw new CommandExecutionException(e);
        }
    }
}
