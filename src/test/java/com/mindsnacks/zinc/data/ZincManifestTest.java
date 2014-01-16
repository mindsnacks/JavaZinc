package com.mindsnacks.zinc.data;

import com.google.gson.Gson;
import com.mindsnacks.zinc.classes.data.ZincManifest;
import com.mindsnacks.zinc.utils.TestFactory;
import com.mindsnacks.zinc.utils.ZincBaseTest;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static com.mindsnacks.zinc.utils.TestFactory.randomString;
import static org.junit.Assert.*;

/**
 * User: NachoSoto
 * Date: 9/10/13
 */
public class ZincManifestTest extends ZincBaseTest {
    private Gson gson;

    private final List<String> mFlavors = Arrays.asList("flavor");
    private final String mIdentifier = "com.mindsnacks.catalog";
    private final String mBundleName = "bundle";
    private final int mVersion = 3;

    @Before
    public void setUp() throws Exception {
        gson = createGson();
    }

    @Test
    public void deserialization() throws Exception {
        final ZincManifest manifest = createSampleManifest();

        assertEquals(mIdentifier, manifest.getIdentifier());
        assertEquals(mBundleName, manifest.getBundleName());
        assertEquals(4, manifest.getFlavors().size());
    }

    @Test
    public void getFilesWithFlavor() throws Exception {
        final ZincManifest manifest = createSampleManifest();

        final Map<String, ZincManifest.FileInfo> result = manifest.getFilesWithFlavor("iphone");
        final ZincManifest.FileInfo info = result.get("level11.mp4");

        assertEquals(1, result.size());
        assertEquals("a7c55929d6f674b839e6ea0276830ee213472952", info.getHash());
    }

    @Test
    public void filePath() throws Exception {
        final ZincManifest.FileInfo info = getFileInfo();

        assertEquals("a7/c5/a7c55929d6f674b839e6ea0276830ee213472952", info.getFilePath());
    }

    @Test
    public void archiveDoesNotExistWithNoFiles() throws Exception {
        assertFalse(createManifest(0).archiveExists(mFlavors.get(0)));
    }

    @Test
    public void archiveDoesNotExistWithOneFile() throws Exception {
        assertFalse(createManifest(1).archiveExists(mFlavors.get(0)));
    }

    @Test
    public void archiveExistsWithTwoFiles() throws Exception {
        assertTrue(createManifest(2).archiveExists(mFlavors.get(0)));
    }

    @Test
    public void containsFilesWithNoFiles() throws Exception {
        assertFalse(createManifest(0).containsFiles(mFlavors.get(0)));
    }

    @Test
    public void containsFilesWithOneFile() throws Exception {
        assertTrue(createManifest(1).containsFiles(mFlavors.get(0)));
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

    private ZincManifest createSampleManifest() {
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
                "  \"catalog\": \"" + mIdentifier + "\",\n" +
                "  \"version\": " + mVersion + ",\n" +
                "  \"bundle\": \"" + mBundleName + "\"\n" +
                "}";

        return gson.fromJson(json, ZincManifest.class);
    }

    private ZincManifest.FileInfo getFileInfo() {
        return createSampleManifest().getFilesWithFlavor("iphone").get("level11.mp4");
    }

    private ZincManifest createManifest(final int count) {
        return new ZincManifest(mFlavors, mIdentifier, mVersion, mBundleName, createFilesMap(count));
    }

    private HashMap<String, ZincManifest.FileInfo> createFilesMap(final int numFiles) {
        final HashMap<String, ZincManifest.FileInfo> result = new HashMap<String, ZincManifest.FileInfo>();

        for (int i = 0; i < numFiles; ++i) {
            result.put(
                    TestFactory.randomString(),
                    new ZincManifest.FileInfo(new HashSet<String>(mFlavors), TestFactory.randomString(), null));
        }

        return result;
    }
}
