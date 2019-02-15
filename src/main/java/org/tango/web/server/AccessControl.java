/*
 * Copyright 2019 Tango Controls
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tango.web.server;

import org.tango.client.ez.proxy.NoSuchCommandException;
import org.tango.client.ez.proxy.TangoProxy;
import org.tango.client.ez.proxy.TangoProxyException;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 01.07.14
 */
public class AccessControl {
    public static final String READ = "read";
    public static final String WRITE = "write";

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

//    @Override
//    public String toString() {
//        return Objects.toString(this)
//                .add("accessMap", accessMap)
//                .add("proxy", proxy)
//                .toString();
//    }
}
