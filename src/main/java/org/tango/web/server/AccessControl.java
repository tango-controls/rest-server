package org.tango.web.server;

import com.google.common.base.Objects;
import org.tango.client.ez.proxy.NoSuchCommandException;
import org.tango.client.ez.proxy.TangoProxies;
import org.tango.client.ez.proxy.TangoProxy;
import org.tango.client.ez.proxy.TangoProxyException;

import java.util.concurrent.*;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 01.07.14
 */
public class AccessControl {
    public static final String READ = "read";
    public static final String WRITE = "write";
    public static final String TANGO_ACCESS = "tango.access_control";

    private final TangoProxy proxy;

    private final ConcurrentMap<String, Future<String>> accessMap = new ConcurrentHashMap<>();

    public AccessControl(TangoProxy proxy) {
        this.proxy = proxy;
    }

    public boolean checkUserCanRead(String userName, String IP, String devName) throws TangoProxyException, NoSuchCommandException {
        String access = getAccess(userName, IP, devName);
        return READ.equals(access) || WRITE.equals(access);
    }

    public boolean checkUserCanWrite(String userName, String IP, String devName) throws TangoProxyException, NoSuchCommandException {
        String access = getAccess(userName, IP, devName);
        return WRITE.equals(access);
    }

    //TODO basically we must subscribe to the events of AddUser etc and cache these values
    private String getAccess(final String userName, final String IP, final String devName) throws TangoProxyException, NoSuchCommandException {
        return proxy.executeCommand("GetAccess", new String[]{userName, IP, devName});
//        final String userKey = userName + "@" + IP;
//        Future<String> f = accessMap.get(userKey);
//        //TODO reset remove task
//        if (f == null) {
//            Callable<String> callable = new Callable<String>() {
//                @Override
//                public String call() throws Exception {
//                    return proxy.executeCommand("GetAccess", new String[]{userName, IP, devName});
//                }
//            };
//            FutureTask<String> ft = new FutureTask<>(callable);
//            f = accessMap.putIfAbsent(userKey, ft);
//            if (f == null) {
//                ft.run();
//                EXEC.schedule(new Runnable() {
//                    @Override
//                    public void run() {
//                        accessMap.remove(userKey);
//                    }
//                }, DEFAULT_CACHE_DELAY, TimeUnit.MINUTES);
//                f = ft;
//            }
//
//        }
//        try {
//            return f.get();
//        } catch (InterruptedException | ExecutionException e) {
//            throw new TangoProxyException(e);
//        }
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("accessMap", accessMap)
                .add("proxy", proxy)
                .toString();
    }
}
