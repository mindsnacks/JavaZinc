package com.zinc.data;

import com.google.gson.Gson;
import com.zinc.classes.data.ZincManifest;
import com.zinc.utils.ZincBaseTest;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * User: NachoSoto
 * Date: 9/10/13
 */
public class ZincManifestTest extends ZincBaseTest {
    private Gson gson;

    @Before
    public void setUp() throws Exception {
        gson = createGson();
    }

    @Test
    public void deserialization() throws Exception {
        final String identifier = "com.mindsnacks.catalog";
        final String bundleName = "bundle name";

        final ZincManifest manifest = createSampleManifest(identifier, bundleName);

        assertEquals(identifier, manifest.getIdentifier());
        assertEquals(bundleName, manifest.getBundleName());
        assertEquals(4, manifest.getFlavors().size());
    }

    @Test
    public void getFilesWithFlavor() throws Exception {
        final ZincManifest manifest = createSampleManifest("identifier", "bundle");

        final Map<String, String> expectedResult = new HashMap<String, String>();
        expectedResult.put("level10.mp4", "7d1bab3be96f71a0c93fab14e0aca05a0ae1167a");

        final Map<String, String> result = manifest.getFilesWithFlavor("iphone");

        assertEquals(expectedResult, result);
    }

    private ZincManifest createSampleManifest(final String identifier, final String bundleName) {
        final String json = "{\n" +
                "  \"flavors\": [\n" +
                "    \"iphone\",\n" +
                "    \"ipad\",\n" +
                "    \"iphone-retina\",\n" +
                "    \"ipad-retina\"\n" +
                "  ],\n" +
                "  \"files\": {\n" +
                "    \"level10.mp4\": {\n" +
                "      \"flavors\": [\n" +
                "        \"iphone\"\n" +
                "      ],\n" +
                "      \"sha\": \"7d1bab3be96f71a0c93fab14e0aca05a0ae1167a\"\n" +
                "    },\n" +
                "    \"level10~ipad.mp4\": {\n" +
                "      \"flavors\": [\n" +
                "        \"ipad\"\n" +
                "      ],\n" +
                "      \"sha\": \"bd90835a496fefedf7b516676edcf5384d089fca\"\n" +
                "    },\n" +
                "    \"level10@2x.mp4\": {\n" +
                "      \"flavors\": [\n" +
                "        \"iphone-retina\"\n" +
                "      ],\n" +
                "      \"sha\": \"bd90835a496fefedf7b516676edcf5384d089fca\"\n" +
                "    },\n" +
                "    \"level10@2x~ipad.mp4\": {\n" +
                "      \"flavors\": [\n" +
                "        \"ipad-retina\"\n" +
                "      ],\n" +
                "      \"sha\": \"b3c83a3db47dba56a73fbd7a97e224a6321d0b12\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"catalog\": \"" + identifier + "\",\n" +
                "  \"version\": 5,\n" +
                "  \"bundle\": \"" + bundleName + "\"\n" +
                "}";

        return gson.fromJson(json, ZincManifest.class);
    }
}
