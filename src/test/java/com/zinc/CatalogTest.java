package com.zinc;

import com.google.gson.Gson;
import org.junit.Before;
import org.junit.Test;
import com.zinc.utils.ZincBaseTest;
import com.zinc.classes.ZincCatalog;

import static org.junit.Assert.assertEquals;

public class CatalogTest extends ZincBaseTest {
    private Gson gson;

    @Before
    public void setUp() throws Exception {
        gson = createGson();
    }

    @Test
    public void createFromJSON() {
        String json = "{\n" +
                "  \"bundles\": {\n" +
                "    \"bundleID1\": {\n" +
                "      \"distributions\": {\n" +
                "        \"master\": 1,\n" +
                "        \"develop\": 2\n" +
                "      }\n" +
                "    },\n" +
                "    \"bundleID2\": {\n" +
                "      \"distributions\": {\n" +
                "        \"master\": 2,\n" +
                "        \"develop\": 3\n" +
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "  \"id\": \"repo\"\n" +
                "}";

        ZincCatalog catalog = gson.fromJson(json, ZincCatalog.class);

        assertEquals("repo", catalog.getIdentifier());
        assertEquals(1, catalog.getVersionForBundleID("bundleID1", "master"));
        assertEquals(2, catalog.getVersionForBundleID("bundleID1", "develop"));
        assertEquals(2, catalog.getVersionForBundleID("bundleID2", "master"));
        assertEquals(3, catalog.getVersionForBundleID("bundleID2", "develop"));
    }
}