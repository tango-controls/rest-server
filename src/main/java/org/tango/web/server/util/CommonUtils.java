package org.tango.web.server.util;

import com.google.common.base.Joiner;
import org.tango.web.server.DatabaseDs;

import java.util.Arrays;
import java.util.List;

/**
 * @author Ingvord
 * @since 06.07.14
 */
public class CommonUtils {
    private CommonUtils() {
    }


    //TODO check performance against regex

    /**
     * Extracts device name from request URI, i.e.
     * <p/>
     * http://localhost:8080/mtango/rest/device/sys/tg_test/1/long_scalar_w --> sys/tg_test/1
     * <p/>
     * if uri contains "devices" a DatabaseDs will be returned, i.e.
     * <p/>
     * http://localhost:8080/mtango/rest/devices --> sys/database/2
     *
     * @param uri
     * @return
     */
    public static String parseDevice(String uri) {
        String[] parts = uri.split("/");
        List<String> partsList = Arrays.asList(parts);
        if (partsList.contains("devices")) return DatabaseDs.DEFAULT_ID;
        int marker = partsList.indexOf("device");
        if(partsList.size() - marker > 3)
            return Joiner.on('/').join(Arrays.copyOfRange(parts, marker + 1, marker + 4));
        else
            return DatabaseDs.DEFAULT_ID;
    }
}
