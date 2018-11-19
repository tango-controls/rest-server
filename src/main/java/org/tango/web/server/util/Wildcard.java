package org.tango.web.server.util;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DeviceProxyFactory;
import org.tango.client.ez.proxy.TangoProxies;
import org.tango.client.ez.proxy.TangoProxyException;
import org.tango.web.server.proxy.TangoDatabaseProxy;
import org.tango.web.server.proxy.TangoDeviceProxy;
import org.tango.web.server.proxy.TangoDeviceProxyImpl;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.tango.web.server.providers.TangoDatabaseProvider.DEFAULT_TANGO_PORT;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11/16/18
 */
public class Wildcard {
    public final Pattern TANGO_HOST_PATTERN = Pattern.compile("\\W+:\\d+");

    public String host;
    public String domain;
    public String family;
    public String member;
    public String attribute;

    public Wildcard() {
    }

    public Wildcard(String host, String domain, String family, String member, String attribute) {
        this.host = host;
        this.domain = domain;
        this.family = family;
        this.member = member;
        this.attribute = attribute;
    }

    public String asDeviceWildcard() {
        //TODO add *???
        return domain + "/" + family + "/" + member;
    }

    public String asFullTangoHost(){
        return TANGO_HOST_PATTERN.matcher(host).matches() ? host : host + ":" + DEFAULT_TANGO_PORT;
    }
}
