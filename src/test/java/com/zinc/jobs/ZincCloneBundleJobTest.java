package com.zinc.jobs;

import com.zinc.classes.data.ZincBundle;
import com.zinc.classes.data.ZincCatalog;
import com.zinc.classes.ZincFutureFactory;
import com.zinc.classes.jobs.ZincCloneBundleJob;
import com.zinc.utils.MockFactory;
import com.zinc.utils.ZincBaseTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Future;

import static com.zinc.utils.MockFactory.randomInt;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * User: NachoSoto
 * Date: 9/5/13
 */
public class ZincCloneBundleJobTest extends ZincBaseTest {
    private ZincCloneBundleJob mJob;

    final private String mBundleID = "com.mindsnacks.games.swell";
    final private String mDistribution = "master";

    final private URL mSourceURL;
    final private String mResultPath = "result path";

    @Mock private ZincCatalog mZincCatalog;
    @Mock private ZincFutureFactory mFutureFactory;
    @Mock private File mRepoFolder;
    @Mock private File mResult;
    private Future<ZincCatalog> mZincCatalogFuture;
    private Future<File> mResultFuture;

    public ZincCloneBundleJobTest() throws MalformedURLException {
        mSourceURL = new URL("https://mindsnacks.com");
    }

    @Before
    public void setUp() throws Exception {
        mZincCatalogFuture = MockFactory.createFutureWithResult(mZincCatalog);
        mResultFuture = MockFactory.createFutureWithResult(mResult);

        when(mFutureFactory.downloadArchive(any(URL.class), any(File.class), anyString())).thenReturn(mResultFuture);
        when(mResult.getPath()).thenReturn(mResultPath);

        mJob = initializeJob(Arrays.asList(mSourceURL));
    }

    private ZincBundle run() throws Exception {
        return mJob.call();
    }

    @Test
    public void getsCatalog() throws Exception {
        run();

        verify(mZincCatalogFuture).get();
    }

    @Test
    public void getsDistributionVersionFromCatalog() throws Exception {
        run();

        verify(mZincCatalog).getVersionForBundleID(mBundleID, mDistribution);
    }

    @Test(expected = ZincCatalog.DistributionNotFoundException.class)
    @SuppressWarnings("unchecked")
    public void throwsIfDistributionIsNotFound() throws Exception {
        when(mZincCatalog.getVersionForBundleID(anyString(), anyString())).thenThrow(ZincCatalog.DistributionNotFoundException.class);

        run();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void downloadsArchive() throws Exception {
        final int version = randomInt(1, 100);

        when(mZincCatalog.getVersionForBundleID(anyString(), anyString())).thenReturn(version);

        // run
        final ZincBundle result = run();

        // verify
        verify(mResultFuture).get();
        verifyDownloadArchiveJobCreation(mSourceURL, version);
        checkResult(result);
    }

    @Test
    public void triesDownloadingArchiveFromAllSources() throws Exception {
        final URL validSourceURL = new URL("http://zinc.mindsnacks.com");
        mJob = initializeJob(Arrays.asList(mSourceURL, validSourceURL));

        final Future<File> resultFuture = MockFactory.createFutureWithExecutionException();

        when(mFutureFactory.downloadArchive(eq(mSourceURL), any(File.class), anyString())).thenReturn(resultFuture);
        when(mFutureFactory.downloadArchive(eq(validSourceURL), any(File.class), anyString())).thenReturn(mResultFuture);

        final int version = randomInt(1, 100);
        when(mZincCatalog.getVersionForBundleID(anyString(), anyString())).thenReturn(version);

        // run
        final ZincBundle result = run();

        // verify
        verifyDownloadArchiveJobCreation(validSourceURL, version);
        checkResult(result);
    }

    private void checkResult(final ZincBundle result) {
        assertEquals(mResultPath, result.getPath());
        assertEquals(mBundleID, result.getBundleID());
    }

    private ZincCloneBundleJob initializeJob(final List<URL> sourceURLs) {
        return new ZincCloneBundleJob(new HashSet<URL>(sourceURLs), mBundleID, mDistribution, mZincCatalogFuture, mFutureFactory, mRepoFolder);
    }

    private void verifyDownloadArchiveJobCreation(final URL validSourceURL, final int version) {
        verify(mFutureFactory).downloadArchive(eq(validSourceURL), eq(mRepoFolder), eq("archives/" + mBundleID + "-" + version));
    }
}
