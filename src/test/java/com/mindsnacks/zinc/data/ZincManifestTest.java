package com.mindsnacks.zinc.data;

import com.google.gson.Gson;
import com.mindsnacks.zinc.classes.data.ZincManifest;
import com.mindsnacks.zinc.utils.ZincBaseTest;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static com.mindsnacks.zinc.utils.MockFactory.randomString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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

        final Map<String, ZincManifest.FileInfo> result = manifest.getFilesWithFlavor("iphone");
        final ZincManifest.FileInfo info = result.get("level11.mp4");

        assertEquals(1, result.size());
        assertEquals("a7c55929d6f674b839e6ea0276830ee213472952", info.getHash());
    }

    @Test
    public void getHashWithExtensionGzippedFile() throws Exception {
        final Map<String, Map<String, Integer>> formats = new HashMap<String, Map<String, Integer>>();
        formats.put(ZincManifest.FileInfo.GZIPPED_FORMAT, null);
        formats.put("raw", null);

        final String hash = randomString();
        final ZincManifest.FileInfo fileInfo = new ZincManifest.FileInfo(null, hash, formats);

        assertTrue(fileInfo.isGzipped());
        assertEquals(hash + "." + ZincManifest.FileInfo.GZIPPED_FORMAT, fileInfo.getHashWithExtension());
    }

    @Test
    public void fileInfoIsNotGzipped() throws Exception {
        final Map<String, Map<String, Integer>> formats = new HashMap<String, Map<String, Integer>>();
        formats.put("raw", null);
        formats.put("other format", null);

        final String hash = randomString();
        final ZincManifest.FileInfo fileInfo = new ZincManifest.FileInfo(null, hash, formats);

        assertFalse(fileInfo.isGzipped());
        assertEquals(hash, new ZincManifest.FileInfo(null, hash, formats).getHashWithExtension());
    }

    private ZincManifest createSampleManifest(final String identifier, final String bundleName) {
        final String json = "{\n" +
                "  \"flavors\": [\n" +
                "    \"ipad-retina\",\n" +
                "    \"iphone\",\n" +
                "    \"ipad\",\n" +
                "    \"iphone-retina\"\n" +
                "  ],\n" +
                "  \"files\": {\n" +
                "    \"level11@2x~ipad.mp4\": {\n" +
                "      \"flavors\": [\n" +
                "        \"ipad-retina\"\n" +
                "      ],\n" +
                "      \"sha\": \"14d4d73af5b4bb4042251f92df785639defd1ff5\",\n" +
                "      \"formats\": {\n" +
                "        \"raw\": {\n" +
                "          \"size\": 386755\n" +
                "        }\n" +
                "      }\n" +
                "    },\n" +
                "    \"level11.mp4\": {\n" +
                "      \"flavors\": [\n" +
                "        \"iphone\"\n" +
                "      ],\n" +
                "      \"sha\": \"a7c55929d6f674b839e6ea0276830ee213472952\",\n" +
                "      \"formats\": {\n" +
                "        \"raw\": {\n" +
                "          \"size\": 122891\n" +
                "        }\n" +
                "      }\n" +
                "    },\n" +
                "    \"level11~ipad.mp4\": {\n" +
                "      \"flavors\": [\n" +
                "        \"ipad\"\n" +
                "      ],\n" +
                "      \"sha\": \"d242110ae6a99ac4b367ad04542624096a90e490\",\n" +
                "      \"formats\": {\n" +
                "        \"raw\": {\n" +
                "          \"size\": 250314\n" +
                "        }\n" +
                "      }\n" +
                "    },\n" +
                "    \"level11@2x.mp4\": {\n" +
                "      \"flavors\": [\n" +
                "        \"iphone-retina\"\n" +
                "      ],\n" +
                "      \"sha\": \"d242110ae6a99ac4b367ad04542624096a90e490\",\n" +
                "      \"formats\": {\n" +
                "        \"raw\": {\n" +
                "          \"size\": 250314\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "  \"catalog\": \"" + identifier + "\",\n" +
                "  \"version\": 3,\n" +
                "  \"bundle\": \"" + bundleName + "\"\n" +
                "}";

        return gson.fromJson(json, ZincManifest.class);
    }
}
