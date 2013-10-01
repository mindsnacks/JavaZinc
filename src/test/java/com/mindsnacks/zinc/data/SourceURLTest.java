package com.mindsnacks.zinc.data;

import com.google.gson.Gson;
import com.mindsnacks.zinc.classes.data.BundleID;
import com.mindsnacks.zinc.classes.data.SourceURL;
import com.mindsnacks.zinc.utils.ZincBaseTest;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;

import static com.mindsnacks.zinc.utils.MockFactory.randomInt;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;

/**
 * User: NachoSoto
 * Date: 9/9/13
 */
public class SourceURLTest extends ZincBaseTest {
    private final URL zincURL;
    private final String catalogID;

    private final Gson mGson = createGson();

    public SourceURLTest() throws MalformedURLException {
        zincURL = new URL("http://zinc.repo.com/some-folder/");
        catalogID = "com.mindsnacks.games";
    }

    @Test
    public void initializeWithHostAndCatalogName() throws Exception {
        assertEquals(catalogID, new SourceURL(zincURL, catalogID).getCatalogID());
    }

    @Test
    public void initializeWithSourceURLSetsTheRightCatalogID() throws Exception {
        assertEquals(catalogID, new SourceURL(new URL(zincURL, catalogID)).getCatalogID());
    }

    @Test
    public void initializeWithHostAndCatalogNameSavesTheURL() throws Exception {
        assertEquals(new URL(zincURL.toString() + catalogID + "/"), new SourceURL(zincURL, catalogID).getUrl());
    }

    @Test
    public void initializeWithSourceURLPreservesTheURL() throws Exception {
        final URL url = new URL(zincURL, catalogID);
        assertEquals(url, new SourceURL(url).getUrl());
    }

    @Test
    public void toStringInitializingWithHostAndCatalogName() throws Exception {
        assertEquals(zincURL + catalogID + "/", new SourceURL(zincURL, catalogID).toString());
    }

    @Test
    public void toStringInitializingSourceURL() throws Exception {
        assertEquals(zincURL + catalogID, new SourceURL(new URL(zincURL, catalogID)).toString());
    }

    @Test
    public void catalogFileURL() throws Exception {
        final URL result = new SourceURL(zincURL, catalogID).getCatalogFileURL();

        assertTrue(result.toString().contains(catalogID));
        assertTrue(result.getFile().endsWith(".json"));
    }

    @Test
    public void manifestFileURL() throws Exception {
        final String bundleName = "swell";
        final int version = randomInt(2, 10);

        final URL result = new SourceURL(zincURL, catalogID).getManifestFileURL(bundleName, version);

        assertTrue(result.toString().contains(catalogID));
        assertTrue(result.toString().contains(Integer.toString(version)));
        assertTrue(result.toString().contains(bundleName));
        assertTrue(result.getFile().endsWith(".json"));
    }

    @Test
    public void archiveURL() throws Exception {
        final String bundleName = "swell";
        final int version = randomInt(1, 1000);
        final String flavorName = "retina";

        final URL result = new SourceURL(zincURL, catalogID).getArchiveURL(bundleName, version, flavorName);

        assertTrue(result.getFile().endsWith(".tar"));
        assertTrue(result.toString().contains(Integer.toString(version)));
        assertTrue(result.toString().contains(bundleName));
        assertTrue(result.toString().contains(flavorName));
    }

    @Test
    public void localDownloadsFolder() throws Exception {
        final BundleID bundleID = new BundleID(catalogID, "swell");
        final int version = randomInt(1, 1000);
        final String flavorName = "retina";

        final String result = SourceURL.getLocalDownloadsFolder(bundleID, version, flavorName);

        assertTrue(result.contains(Integer.toString(version)));
        assertTrue(result.contains(bundleID.toString()));
        assertTrue(result.contains(flavorName));
    }

    @Test
    public void localBundlesFolder() throws Exception {
        final BundleID bundleID = new BundleID(catalogID, "swell");
        final int version = randomInt(1, 1000);
        final String flavorName = "retina";

        final String result = SourceURL.getLocalBundlesFolder(bundleID, version, flavorName);

        assertTrue(result.contains(Integer.toString(version)));
        assertTrue(result.contains(bundleID.toString()));
        assertTrue(result.contains(flavorName));
    }

    @Test
    public void localBundlesFolderIsDifferentThanDownloads() throws Exception {
        final BundleID bundleID = new BundleID(catalogID, "swell");
        final int version = randomInt(1, 1000);
        final String flavorName = "retina";

        assertThat(SourceURL.getLocalBundlesFolder(bundleID, version, flavorName), not(equalTo(SourceURL.getLocalDownloadsFolder(bundleID, version, flavorName))));
    }

    @Test
    public void testTrailingSlashes() throws Exception {
        final String catalogID = "com.mindsnacks.misc";

        final SourceURL result = new SourceURL(new URL("http://zinc.mindsnacks.com/" + catalogID + "/"));

        assertEquals(catalogID, result.getCatalogID());
    }

    @Test
    public void serialization() throws Exception {
        final SourceURL sourceURL = new SourceURL(zincURL, catalogID);

        final String json = mGson.toJson(sourceURL).replace("\"", "");

        assertEquals(sourceURL.getUrl().toString(), json);
    }

    @Test
    public void deserialization() throws Exception {
        final URL url = new URL(zincURL, catalogID);

        final SourceURL sourceURL = mGson.fromJson("\"" + url + "\"", SourceURL.class);

        assertEquals(catalogID, sourceURL.getCatalogID());
        assertEquals(url, sourceURL.getUrl());
    }
}
