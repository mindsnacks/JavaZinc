package com.mindsnacks.zinc.repo;

import com.mindsnacks.zinc.classes.data.*;
import com.mindsnacks.zinc.exceptions.ZincRuntimeException;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;

import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.*;

/**
 * User: NachoSoto
 * Date: 9/3/13
 */
public class ZincRepoInitializationTest extends ZincRepoBaseTest {
    private final SourceURL mSourceURL;

    @Mock private ZincBundle mResultBundle;
    @Mock private BundleID mBundleID;

    public ZincRepoInitializationTest() throws MalformedURLException {
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
        writeSourceURLsToIndexFile(Arrays.asList(mSourceURL));
        initializeRepo();

        // run
        final SourceURL newSourceURL = new SourceURL(new URL("https://mindsnacks.com"), "com.mindsnacks.games");
        mRepo.addSourceURL(newSourceURL);

        // check
        assertEquals(new HashSet<SourceURL>(Arrays.asList(mSourceURL, newSourceURL)), readRepoIndex().getSources());
    }

    @Test
    public void addingTrackingRequestAddsItToIndexFile() throws IOException, ZincRepoIndex.BundleNotBeingTrackedException {
        // run
        final String catalogID = "com.mindsnacks.games";
        final BundleID bundleID = new BundleID(catalogID, "swell");
        final String distribution = "master";

        mRepo.addSourceURL(new SourceURL(new URL("https://mindsnacks.com"), catalogID));
        mRepo.startTrackingBundle(bundleID, distribution);

        // verify
        final ZincRepoIndex.TrackingInfo trackingInfo = readRepoIndex().getTrackingInfo(bundleID);
        Assert.assertEquals(distribution, trackingInfo.getDistribution());
    }

    @Test
    public void bundleIsNotValidIfNotTracked() {
        when(mResultBundle.getBundleID()).thenReturn(mBundleID);
        assertFalse(mRepo.isBundleValid(mResultBundle));
    }

    @Test(expected = ZincRuntimeException.class)
    public void notAllowedToRetrackABundleThatIsNotAlreadyTracked() {
        when(mResultBundle.getBundleID()).thenReturn(mBundleID);
        mRepo.retrackBundle(mResultBundle);
    }

    @Test
    public void clearCachedCatalogs() throws Exception {
        mRepo.clearCachedCatalogs();

        verify(mCatalogsCache).clearCachedCatalogs();
    }

    private void writeSourceURLsToIndexFile(final List<SourceURL> newSourceURL) throws IOException {
        final FileWriter fileWriter = new FileWriter(getIndexFile());
        fileWriter.write(String.format("{\"sources\": %s}", mGson.toJson(newSourceURL)));
        fileWriter.close();
    }
}
