package zinc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Before;
import org.junit.Test;
import zinc.Catalog;

import static org.junit.Assert.assertEquals;

public class CatalogTests {
    private Gson gson;

    @Before
    public void setUp() throws Exception {
        gson = new GsonBuilder().setPrettyPrinting().create();
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

        Catalog catalog = gson.fromJson(json, Catalog.class);

        assertEquals("repo", catalog.getIdentifier());
        assertEquals(1, catalog.getInfoForBundleID("bundleID1").getVersionForDistribution("master"));
        assertEquals(2, catalog.getInfoForBundleID("bundleID1").getVersionForDistribution("develop"));
        assertEquals(2, catalog.getInfoForBundleID("bundleID2").getVersionForDistribution("master"));
        assertEquals(3, catalog.getInfoForBundleID("bundleID2").getVersionForDistribution("develop"));
    }
}