package org.tango.web.server;

import org.tango.client.ez.proxy.TangoProxies;
import org.tango.client.ez.proxy.TangoProxy;
import org.tango.client.ez.proxy.TangoProxyException;

import java.util.concurrent.*;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 01.07.14
 */
public class AccessControl {
    public static final String DEFAULT_ID = "sys/access_control/1";

    public static final String READ = "read";
    public static final String WRITE = "write";
    public static final String TANGO_ACCESS = "tango.access_control";
    private static final long DEFAULT_CACHE_DELAY = 5;

    private final TangoProxy proxy;

    private static final ScheduledExecutorService EXEC = Executors.newScheduledThreadPool(1);

    private final ConcurrentMap<String, Future<String>> accessMsp = new ConcurrentHashMap<>();

    public AccessControl(String tangoHost) throws TangoProxyException {
        this(tangoHost, DEFAULT_ID);
    }

    public AccessControl(String tangoHost, String devId) throws TangoProxyException {
        this(TangoProxies.newDeviceProxyWrapper("tango://" + tangoHost + "/" + devId));
    }

    public AccessControl(TangoProxy proxy) {
        this.proxy = proxy;
    }

    public boolean checkUserCanRead(String userName, String IP, String devName) throws TangoProxyException {
        String access = getAccess(userName, IP, devName);
        return READ.equals(access) || WRITE.equals(access);
    }

    public boolean checkUserCanWrite(String userName, String IP, String devName) throws TangoProxyException {
        String access = getAccess(userName, IP, devName);
        return WRITE.equals(access);
    }

    private String getAccess(final String userName, final String IP, final String devName) throws TangoProxyException {
        final String userKey = userName + "@" + IP;
        Future<String> f = accessMsp.get(userKey);
        //TODO reset remove task
        if (f == null) {
            Callable<String> callable = new Callable<String>() {
                @Override
                public String call() throws Exception {
                    return proxy.executeCommand("GetAccess", new String[]{userName, IP, devName});
                }
            };
            FutureTask<String> ft = new FutureTask<>(callable);
            f = accessMsp.putIfAbsent(userKey, ft);
            if (f == null) {
                ft.run();
                EXEC.schedule(new Runnable() {
                    @Override
                    public void run() {
                        accessMsp.remove(userKey);
                    }
                }, DEFAULT_CACHE_DELAY, TimeUnit.MINUTES);
                f = ft;
            }

        }
        try {
            return f.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new TangoProxyException(e);
        }
    }
}
