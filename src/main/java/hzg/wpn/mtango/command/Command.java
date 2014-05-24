package hzg.wpn.mtango.command;

import hzg.wpn.tango.client.proxy.TangoProxy;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 15.10.12
 */
@Immutable
public class Command {
    private final TangoProxy proxy;
    private final Method method;
    private final Object[] args;


    public Command(@Nonnull TangoProxy proxy, @Nonnull Method method, Object... args) {
        this.proxy = proxy;
        this.method = method;
        this.args = args;
    }

    public Object execute() throws Exception {
        return method.invoke(proxy, args);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Command other = (Command) o;

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
