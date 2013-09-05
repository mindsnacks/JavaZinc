package com.zinc.utils;

import com.zinc.classes.ZincCatalog;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

/**
 * User: NachoSoto
 * Date: 9/3/13
 */
public final class MockFactory {
    private static final SecureRandom random = new SecureRandom();

    public static String randomString() {
        return new BigInteger(130, random).toString(32);
    }

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
}
