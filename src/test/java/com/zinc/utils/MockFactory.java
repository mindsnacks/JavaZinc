package com.zinc.utils;

import com.zinc.classes.data.ZincCatalog;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * User: NachoSoto
 * Date: 9/3/13
 */
public final class MockFactory {
    private static final SecureRandom random = new SecureRandom();

    public static ZincCatalog createCatalog() {
        final Map<String, Integer> distributions = new HashMap<String, Integer>();
        distributions.put("master", 2);
        distributions.put("develop", 1);

        final Map<String, ZincCatalog.Info> bundles = new HashMap<String, ZincCatalog.Info>();
        bundles.put("bundle1", new ZincCatalog.Info(distributions));

        return new ZincCatalog(randomString(), bundles);
    }

    public static InputStream inputStreamWithString(final String string) {
        return new ByteArrayInputStream(string.getBytes());
    }

    public static <V> Future<V> createFutureWithResult(V result) {
        final Future<V> mock = futureMock();

        return setFutureResult(mock, result);
    }

    public static <V> Future<V> setFutureResult(Future<V> mock, V result) {
        try {
            when(mock.get()).thenReturn(result);
        }
        catch (InterruptedException thisIsJustAMock) {}
        catch (ExecutionException thisIsJustAMock) {}

        return mock;
    }

    @SuppressWarnings("unchecked")
    public static <V> Future<V> createFutureWithExecutionException() {
        final Future<V> mock = futureMock();
        try {
            when(mock.get()).thenThrow(ExecutionException.class);
        }
        catch (InterruptedException thisIsJustAMock) {}
        catch (ExecutionException thisIsJustAMock) {}

        return mock;
    }

    @SuppressWarnings("unchecked")
    private static <V> Future<V> futureMock() {
        return mock(Future.class);
    }

    @SuppressWarnings("unchecked")
    public static <V> Callable<V> callableWithResult(V result) {
        final Callable<V> mock = mock(Callable.class);
        try {
            when(mock.call()).thenReturn(result);
        } catch (Exception thisCantHappen) { assert false; }

        return mock;
    }

    public static int randomInt(int min, int max) {
        return random.nextInt(max) + min;
    }

    public static String randomString() {
        return new BigInteger(130, random).toString(32);
    }

    public static int randomInt() {
        return random.nextInt();
    }

    public static class DaemonThreadFactory implements ThreadFactory {
        @Override
        public Thread newThread(final Runnable r) {
            final Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        }
    }
}
