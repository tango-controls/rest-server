package org.tango.server;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import fr.esrf.Tango.DevFailed;
import org.tango.client.database.DatabaseFactory;
import org.tango.client.ez.util.TangoUtils;
import org.tango.server.export.IExporter;
import org.tango.server.servant.DeviceImpl;
import org.tango.utils.DevFailedUtils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 04.02.2016
 */
public class ServerManagerUtils {
    private ServerManagerUtils(){}

    /**
     *
     * @param instance
     * @param clazz
     * @param <T>
     * @return collections that contains business objects of the Tango instance
     * @throws java.lang.RuntimeException
     */
    public static <T> List<T> getBusinessObjects(String instance, final Class<T> clazz){
        try {
            Field tangoExporterField = ServerManager.getInstance().getClass().getDeclaredField("tangoExporter");
            tangoExporterField.setAccessible(true);
            final IExporter tangoExporter = (IExporter) tangoExporterField.get(ServerManager.getInstance());

            final String[] deviceList = DatabaseFactory.getDatabase().getDeviceList(
                    clazz.getSimpleName() + "/" + instance, clazz.getSimpleName());

            if (deviceList.length == 0) //No tango devices were found. Simply skip the following
                return Collections.emptyList();

            return Lists.newArrayList(Iterables.filter(Lists.transform(Arrays.asList(deviceList), new Function<String, T>() {
                @Override
                public T apply(String input) {
                    try {
                        DeviceImpl deviceImpl = tangoExporter.getDevice(clazz.getSimpleName(), input);
                        return (T) deviceImpl.getBusinessObject();
                    } catch (DevFailed devFailed) {
                        return null;
                    }
                }
            }), Predicates.notNull()));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (DevFailed devFailed) {
            throw new RuntimeException(TangoUtils.convertDevFailedToException(devFailed));
        }
    }
}
