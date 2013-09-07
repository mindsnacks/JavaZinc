package com.zinc.repo;

import org.junit.Assert;
import org.junit.Test;
import com.zinc.classes.ZincRepoIndex;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * User: NachoSoto
 * Date: 9/3/13
 */
public class RepoInitializationTest extends RepoBaseTest {
    @Test
    public void addingSourceURLCreatesIndexFile() throws MalformedURLException, FileNotFoundException {
        final URL catalogURL = new URL("http://www.google.com");

        // run
        mRepo.addSourceURL(catalogURL);

        // check
        assertTrue(readRepoIndex().getSources().contains(catalogURL));
    }

    @Test
    public void addingSourceURLsAddsAllOfThem() throws MalformedURLException, FileNotFoundException {
        final URL catalogURL1 = new URL("http://www.google.com"),
                  catalogURL2 = new URL("http://www.mindsnacks.com");

        // run
        mRepo.addSourceURL(catalogURL1);
        mRepo.addSourceURL(catalogURL2);

        // check
        assertTrue(readRepoIndex().getSources().contains(catalogURL1));
        assertTrue(readRepoIndex().getSources().contains(catalogURL2));
    }

    @Test
    public void addingSourceURLAddsItToIndexFile() throws IOException {
        final URL originalSourceURL = new URL("https://www.google.com/test"),
                  newSourceURL = new URL("https://www.mindsnacks.com/");

        final FileWriter fileWriter = new FileWriter(getIndexFile());

        fileWriter.write(String.format("{\"sources\": [\"%s\"]}", originalSourceURL));
        fileWriter.close();

        // run
        mRepo.addSourceURL(newSourceURL);

        // check
        assertEquals(new HashSet<URL>(Arrays.asList(originalSourceURL, newSourceURL)), readRepoIndex().getSources());
    }

    @Test
    public void addingTrackingRequestAddsItToIndexFile() throws FileNotFoundException {
        // run
        final String bundleID = "com.mindsnacks.games.swell",
                     distribution = "master";

        mRepo.startTrackingBundle(bundleID, distribution);

        // verify
        final ZincRepoIndex.TrackingInfo trackingInfo = readRepoIndex().getTrackingInfo(bundleID);
        Assert.assertEquals(distribution, trackingInfo.getDistribution());
    }
}
