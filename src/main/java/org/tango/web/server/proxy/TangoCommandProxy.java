package org.tango.web.server.proxy;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DeviceProxy;
import fr.soleil.tango.clientapi.TangoCommand;

import java.util.List;

/**
 * @author ingvord
 * @since 11/18/18
 */
public interface TangoCommandProxy {
    String getCommandName();

    void execute() throws DevFailed;

    void execute(Object value) throws DevFailed;

    void execute(Object... value) throws DevFailed;

    Object executeExtract(Object value) throws DevFailed;

    <T> T execute(Class<T> clazz) throws DevFailed;

    Number executeExtractNumber() throws DevFailed;

    <T> List<T> executeExtractList(Class<T> clazz) throws DevFailed;

    Number[] executeExtractNumberArray() throws DevFailed;

    <T> T execute(Class<T> clazz, Object value) throws DevFailed;

    <T> T execute(Class<T> clazz, Object... value) throws DevFailed;

    Number executeExtractNumber(Object value) throws DevFailed;

    <T> List<T> executeExtractList(Class<T> clazz, Object value) throws DevFailed;

    <T> List<T> executeExtractList(Class<T> clazz, Object... value) throws DevFailed;

    void insertMixArgin(String[] numberArgin, String[] stringArgin) throws DevFailed;

    void insertMixArgin(double[] numberArgin, String[] stringArgin) throws DevFailed;

    void insertMixArgin(int[] numberArgin, String[] stringArgin) throws DevFailed;

    String extractToString(String separator) throws DevFailed;

    String[] getNumMixArrayArgout() throws DevFailed;

    String[] getStringMixArrayArgout() throws DevFailed;

    double[] getNumDoubleMixArrayArgout() throws DevFailed;

    int[] getNumLongMixArrayArgout() throws DevFailed;

    boolean isArginScalar() throws DevFailed;

    boolean isArginSpectrum() throws DevFailed;

    boolean isArginVoid() throws DevFailed;

    boolean isArgoutVoid() throws DevFailed;

    boolean isArgoutSpectrum() throws DevFailed;

    boolean isArginMixFormat() throws DevFailed;

    boolean isArgoutScalar() throws DevFailed;

    boolean isArgoutMixFormat() throws DevFailed;

    DeviceProxy getDeviceProxy();

    int getArginType() throws DevFailed;

    int getArgoutType() throws DevFailed;

    void setTimeout(int timeout) throws DevFailed;

    TangoCommand asTangoCommand();
}
