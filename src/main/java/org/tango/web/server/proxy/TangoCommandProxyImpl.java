package org.tango.web.server.proxy;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DeviceProxy;
import fr.soleil.tango.clientapi.TangoCommand;

import java.util.List;

/**
 * @author ingvord
 * @since 11/18/18
 */
public class TangoCommandProxyImpl implements TangoCommandProxy {
    private final TangoCommand command;

    public TangoCommandProxyImpl(TangoCommand command) {
        this.command = command;
    }

    @Override
    public String getCommandName() {
        return command.getCommandName();
    }

    @Override
    public void execute() throws DevFailed {
        command.execute();
    }

    @Override
    public void execute(Object value) throws DevFailed {
        command.execute(value);
    }

    @Override
    public void execute(Object... value) throws DevFailed {
        command.execute(value);
    }

    @Override
    public Object executeExtract(Object value) throws DevFailed {
        return command.executeExtract(value);
    }

    @Override
    public <T> T execute(Class<T> clazz) throws DevFailed {
        return command.execute(clazz);
    }

    @Override
    public Number executeExtractNumber() throws DevFailed {
        return command.executeExtractNumber();
    }

    @Override
    public <T> List<T> executeExtractList(Class<T> clazz) throws DevFailed {
        return command.executeExtractList(clazz);
    }

    @Override
    public Number[] executeExtractNumberArray() throws DevFailed {
        return command.executeExtractNumberArray();
    }

    @Override
    public <T> T execute(Class<T> clazz, Object value) throws DevFailed {
        return command.execute(clazz, value);
    }

    @Override
    public <T> T execute(Class<T> clazz, Object... value) throws DevFailed {
        return command.execute(clazz, value);
    }

    @Override
    public Number executeExtractNumber(Object value) throws DevFailed {
        return command.executeExtractNumber(value);
    }

    @Override
    public <T> List<T> executeExtractList(Class<T> clazz, Object value) throws DevFailed {
        return command.executeExtractList(clazz, value);
    }

    @Override
    public <T> List<T> executeExtractList(Class<T> clazz, Object... value) throws DevFailed {
        return command.executeExtractList(clazz, value);
    }

    @Override
    public void insertMixArgin(String[] numberArgin, String[] stringArgin) throws DevFailed {
        command.insertMixArgin(numberArgin, stringArgin);
    }

    @Override
    public void insertMixArgin(double[] numberArgin, String[] stringArgin) throws DevFailed {
        command.insertMixArgin(numberArgin, stringArgin);
    }

    @Override
    public void insertMixArgin(int[] numberArgin, String[] stringArgin) throws DevFailed {
        command.insertMixArgin(numberArgin, stringArgin);
    }

    @Override
    public String extractToString(String separator) throws DevFailed {
        return command.extractToString(separator);
    }

    @Override
    public String[] getNumMixArrayArgout() throws DevFailed {
        return command.getNumMixArrayArgout();
    }

    @Override
    public String[] getStringMixArrayArgout() throws DevFailed {
        return command.getStringMixArrayArgout();
    }

    @Override
    public double[] getNumDoubleMixArrayArgout() throws DevFailed {
        return command.getNumDoubleMixArrayArgout();
    }

    @Override
    public int[] getNumLongMixArrayArgout() throws DevFailed {
        return command.getNumLongMixArrayArgout();
    }

    @Override
    public boolean isArginScalar() throws DevFailed {
        return command.isArginScalar();
    }

    @Override
    public boolean isArginSpectrum() throws DevFailed {
        return command.isArginSpectrum();
    }

    @Override
    public boolean isArginVoid() throws DevFailed {
        return command.isArginVoid();
    }

    @Override
    public boolean isArgoutVoid() throws DevFailed {
        return command.isArgoutVoid();
    }

    @Override
    public boolean isArgoutSpectrum() throws DevFailed {
        return command.isArgoutSpectrum();
    }

    @Override
    public boolean isArginMixFormat() throws DevFailed {
        return command.isArginMixFormat();
    }

    @Override
    public boolean isArgoutScalar() throws DevFailed {
        return command.isArgoutScalar();
    }

    @Override
    public boolean isArgoutMixFormat() throws DevFailed {
        return command.isArgoutMixFormat();
    }

    @Override
    public DeviceProxy getDeviceProxy() {
        return command.getDeviceProxy();
    }

    @Override
    public int getArginType() throws DevFailed {
        return command.getArginType();
    }

    @Override
    public int getArgoutType() throws DevFailed {
        return command.getArgoutType();
    }

    @Override
    public void setTimeout(int timeout) throws DevFailed {
        command.setTimeout(timeout);
    }

    @Override
    public TangoCommand asTangoCommand() {
        return command;
    }
}
