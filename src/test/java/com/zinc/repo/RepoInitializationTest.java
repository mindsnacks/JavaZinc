package com.zinc.repo;

import com.zinc.classes.data.BundleID;
import com.zinc.classes.data.SourceURL;
import com.zinc.classes.data.ZincRepoIndex;
import org.junit.Assert;
import org.junit.Test;

import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * User: NachoSoto
 * Date: 9/3/13
 */
public class RepoInitializationTest extends RepoBaseTest {
    private final SourceURL mSourceURL;

    public RepoInitializationTest() throws MalformedURLException {
        mSourceURL = new SourceURL(new URL("https://mindsnacks.com"), "com.mindsnacks.lessons");
    }

    @Test
    public void addingSourceURLCreatesIndexFile() throws IOException {
        // run
        mRepo.addSourceURL(mSourceURL);

        // check
        assertTrue(readRepoIndex().getSources().contains(mSourceURL));
    }

    @Test
    public void addingSourceURLsAddsAllOfThem() throws IOException {
        final SourceURL sourceURL2 = new SourceURL(new URL("https://mindsnacks.com"), "com.mindsnacks.lessons");

        // run
        mRepo.addSourceURL(mSourceURL);
        mRepo.addSourceURL(sourceURL2);

        // check
        assertTrue(readRepoIndex().getSources().contains(mSourceURL));
        assertTrue(readRepoIndex().getSources().contains(sourceURL2));
    }

    @Test
    public void addingSourceURLAddsItToIndexFile() throws IOException {
        final SourceURL newSourceURL = new SourceURL(new URL("https://mindsnacks.com"), "com.mindsnacks.games");

        writeSourceURLsToIndexFile(Arrays.asList(mSourceURL));

        // run
        mRepo.addSourceURL(newSourceURL);

        // check
        assertEquals(new HashSet<SourceURL>(Arrays.asList(mSourceURL, newSourceURL)), readRepoIndex().getSources());
    }

    @Test
    public void addingTrackingRequestAddsItToIndexFile() throws IOException {
        // run
        final BundleID bundleID = new BundleID("com.mindsnacks.games.swell");
        final String distribution = "master";

        mRepo.startTrackingBundle(bundleID, distribution);

        // verify
        final ZincRepoIndex.TrackingInfo trackingInfo = readRepoIndex().getTrackingInfo(bundleID);
        Assert.assertEquals(distribution, trackingInfo.getDistribution());
    }

    private void writeSourceURLsToIndexFile(final List<SourceURL> newSourceURL) throws IOException {
        final FileWriter fileWriter = new FileWriter(getIndexFile());
        fileWriter.write(String.format("{\"sources\": %s}", mGson.toJson(newSourceURL)));
        fileWriter.close();
    }
}
