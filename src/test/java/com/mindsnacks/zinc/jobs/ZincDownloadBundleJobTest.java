package com.mindsnacks.zinc.jobs;

import com.mindsnacks.zinc.classes.ZincJobFactory;
import com.mindsnacks.zinc.classes.data.*;
import com.mindsnacks.zinc.classes.jobs.ZincDownloadBundleJob;
import com.mindsnacks.zinc.utils.TestFactory;
import com.mindsnacks.zinc.utils.ZincBaseTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import static com.mindsnacks.zinc.utils.TestFactory.randomInt;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * User: NachoSoto
 * Date: 9/5/13
 */
public class ZincDownloadBundleJobTest extends ZincBaseTest {
    private ZincDownloadBundleJob mJob;

    final private String mBundleName = "swell";
    final private String mCatalogID = "com.mindsnacks.games";
    final private BundleID mBundleID = new BundleID(mCatalogID, mBundleName);
    final private String mDistribution = "master";
    final private String mFlavorName = "retina";
    final private int mVersion = randomInt(1, 100);
    final private URL mSourceHost = TestFactory.createURL("https://mindsnacks.com/");
    final private URL mArchiveURL;
    final private String mResultPath = "result path";

    @Mock private SourceURL mSourceURL;
    @Mock private ZincCatalog mCatalog;
    @Mock private Future<ZincCatalog> mCatalogFuture;
    @Mock private ZincJobFactory mJobFactory;
    @Mock private File mRepoFolder;
    @Mock private File mResult;

    // we always want to override in case a corrupt bundle is in place
    // if this job is being run it means the result bundle wasn't there anyway.
    private static boolean mShouldOverrideDownloadedBundle = true;

    private Callable<File> mResultJob;

    public ZincDownloadBundleJobTest() throws MalformedURLException {
        mArchiveURL = new URL(mSourceHost, mCatalogID + "/" + mBundleName + "-" + mDistribution + "-" + mFlavorName);
    }

    @Before
    public void setUp() throws Exception {
        when(mSourceURL.getUrl()).thenReturn(mSourceHost);
        when(mSourceURL.getCatalogID()).thenReturn(mCatalogID);
        when(mSourceURL.getArchiveURL(eq(mBundleName), anyInt(), eq(mFlavorName))).thenReturn(mArchiveURL);

        TestFactory.setFutureResult(mCatalogFuture, mCatalog);

        mResultJob = TestFactory.createCallable(mResult);

        when(mJobFactory.downloadArchive(any(URL.class), any(File.class), anyString(), eq(mShouldOverrideDownloadedBundle))).thenReturn(mResultJob);
        when(mResult.getPath()).thenReturn(mResultPath);

        mJob = initializeJob(mSourceURL);
    }

    private ZincBundle run() throws Exception {
        return mJob.call();
    }

    @Test
    public void getsCatalog() throws Exception {
        run();

        verify(mCatalogFuture).get();
    }

    @Test
    public void getsDistributionVersionFromCatalog() throws Exception {
        run();

        verify(mCatalog).getVersionForBundleName(mBundleName, mDistribution);
    }

    @Test(expected = ZincCatalog.DistributionNotFoundException.class)
    @SuppressWarnings("unchecked")
    public void throwsIfDistributionIsNotFound() throws Exception {
        when(mCatalog.getVersionForBundleName(anyString(), anyString())).thenThrow(ZincCatalog.DistributionNotFoundException.class);

        run();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void downloadsArchive() throws Exception {
        when(mCatalog.getVersionForBundleName(anyString(), anyString())).thenReturn(mVersion);

        // run
        final ZincBundle result = run();

        // verify
        verify(mResultJob).call();
        verifyDownloadArchiveJobCreation();
        checkResult(result);
    }

    private void checkResult(final ZincBundle result) {
        assertEquals(mResultPath, result.getPath());
        assertEquals(mBundleID, result.getBundleID());
        assertEquals(mVersion, result.getVersion());
    }

    private ZincDownloadBundleJob initializeJob(final SourceURL sourceURL) {
        return new ZincDownloadBundleJob(
                new ZincCloneBundleRequest(sourceURL, mBundleID, mDistribution, mFlavorName, mRepoFolder),
                mJobFactory,
                mCatalogFuture);
    }

    private void verifyDownloadArchiveJobCreation() {
        verify(mJobFactory).downloadArchive(eq(mArchiveURL), eq(mRepoFolder), anyString(), eq(mShouldOverrideDownloadedBundle));
    }
}
