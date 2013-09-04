package utils;

import zinc.classes.ZincCatalog;

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
        return createCatalogWithIdentifier(randomString());
    }

    public static ZincCatalog createCatalogWithIdentifier(String identifier) {
        final Map<String, Integer> distributions = new HashMap<String, Integer>();
        distributions.put("master", 2);
        distributions.put("develop", 1);

        final Map<String, ZincCatalog.Info> bundles = new HashMap<String, ZincCatalog.Info>();
        bundles.put("bundle1", new ZincCatalog.Info(distributions));

        return new ZincCatalog(identifier, bundles);
    }
}
