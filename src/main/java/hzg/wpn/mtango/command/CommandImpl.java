package hzg.wpn.mtango.command;

import wpn.hdri.tango.proxy.TangoProxyWrapper;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 15.10.12
 */
public class CommandImpl implements Command {
    private final TangoProxyWrapper proxy;
    private final Method method;
    private final Object[] args;


    public CommandImpl(@Nonnull TangoProxyWrapper proxy, @Nonnull Method method, Object... args) {
        this.proxy = proxy;
        this.method = method;
        this.args = args;
    }

    public Object execute() throws CommandExecutionException {
        try {
            return method.invoke(proxy, args);
        } catch (Exception e) {
            throw new CommandExecutionException(e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CommandImpl other = (CommandImpl) o;

        if (proxy != other.proxy) return false;
        if (!method.equals(other.method)) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(args, other.args)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = 17;
        //avoiding possible zero value
        result = 31 * result + proxy.hashCode();
        result = 31 * result + (method.hashCode());
        result = 31 * result + (args != null ? Arrays.hashCode(args) : 0);
        return result;
    }
}
