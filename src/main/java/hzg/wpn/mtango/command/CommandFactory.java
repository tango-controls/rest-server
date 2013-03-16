package hzg.wpn.mtango.command;

import wpn.hdri.tango.proxy.TangoProxyWrapper;

import java.lang.reflect.Method;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11.10.12
 */
public class CommandFactory {
    public Command createCommand(CommandInfo info, TangoProxyWrapper proxy) throws CommandCreationException {
        CommandType type = CommandType.valueOf(info.type.toUpperCase());
        switch (type) {
            case READ:
                return createReadCommand(info, proxy);
            case WRITE:
                return createWriteCommand(info, proxy);
            case EXECUTE:
                return createExecCommand(info, proxy);
            default:
                throw new CommandCreationException(new IllegalStateException());
        }
    }

    public Command createReadCommand(CommandInfo info, TangoProxyWrapper proxy) throws CommandCreationException {
        try {
            Method method = proxy.getClass().getMethod("readAttributeValueAndTime", String.class);
            String attributeName = info.target;

            return new CommandImpl(proxy, method, attributeName);
        } catch (NoSuchMethodException e) {
            throw new CommandCreationException(e);
        }
    }

    public Command createWriteCommand(CommandInfo info, TangoProxyWrapper proxy) throws CommandCreationException {
        try {
            Method method = proxy.getClass().getMethod("writeAttribute", String.class, Object.class);
            String attributeName = info.target;
            Object arg = info.convertArgin(proxy.getAttributeInfo(attributeName).getType().getDataType());

            return new CommandImpl(proxy, method, attributeName, arg);
        } catch (NoSuchMethodException e) {
            throw new CommandCreationException(e);
        }
    }

    public Command createExecCommand(CommandInfo info, TangoProxyWrapper proxy) throws CommandCreationException {
        try {
            Method method = proxy.getClass().getMethod("executeCommand", String.class, Object.class);
            String cmdName = info.target;
            Object arg = info.convertArgin(proxy.getCommandInfo(cmdName).getArginType());

            return new CommandImpl(proxy, method, cmdName, arg);
        } catch (NoSuchMethodException e) {
            throw new CommandCreationException(e);
        }
    }
}
