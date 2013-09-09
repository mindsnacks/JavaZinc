package com.zinc.data;

import com.google.gson.Gson;
import com.zinc.classes.data.SourceURL;
import com.zinc.utils.ZincBaseTest;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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

    @Test
    public void catalogFileURL() throws Exception {
        final URL result = new SourceURL(zincURL, catalogID).getCatalogFileURL();

        assertTrue(result.toString().contains(catalogID));
        assertTrue(result.getFile().endsWith(".json"));
    }
}
