package com.zinc.jobs;

import com.zinc.classes.ZincFutureFactory;
import com.zinc.classes.data.*;
import com.zinc.classes.jobs.ZincCloneBundleJob;
import com.zinc.utils.MockFactory;
import com.zinc.utils.ZincBaseTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
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

    final private String mBundleName = "swell";
    final private String mCatalogID = "com.mindsnacks.games";
    final private BundleID mBundleID = new BundleID(mCatalogID, mBundleName);
    final private String mDistribution = "master";
    final private String mFlavorName = "retina";
    final private URL mSourceHost;
    final private URL mArchiveURL;

    final private String mResultPath = "result path";
    @Mock private SourceURL mSourceURL;
    @Mock private ZincCatalog mZincCatalog;
    @Mock private ZincFutureFactory mFutureFactory;
    @Mock private File mRepoFolder;
    @Mock private File mResult;
    private Future<ZincCatalog> mZincCatalogFuture;
    private Future<File> mResultFuture;

    public ZincCloneBundleJobTest() throws MalformedURLException {
        mSourceHost = new URL("https://mindsnacks.com/");
        mArchiveURL = new URL(mSourceHost, mCatalogID + "/" + mBundleName + "-" + mDistribution + "-" + mFlavorName);
    }

    @Before
    public void setUp() throws Exception {
        when(mSourceURL.getUrl()).thenReturn(mSourceHost);
        when(mSourceURL.getCatalogID()).thenReturn(mCatalogID);
        when(mSourceURL.getArchiveURL(eq(mBundleName), anyInt(), eq(mFlavorName))).thenReturn(mArchiveURL);

        mZincCatalogFuture = MockFactory.createFutureWithResult(mZincCatalog);
        mResultFuture = MockFactory.createFutureWithResult(mResult);

        when(mFutureFactory.downloadArchive(any(URL.class), any(File.class), anyString(), eq(false))).thenReturn(mResultFuture);
        when(mResult.getPath()).thenReturn(mResultPath);

        mJob = initializeJob(mSourceURL);
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

        verify(mZincCatalog).getVersionForBundleName(mBundleName, mDistribution);
    }

    @Test(expected = ZincCatalog.DistributionNotFoundException.class)
    @SuppressWarnings("unchecked")
    public void throwsIfDistributionIsNotFound() throws Exception {
        when(mZincCatalog.getVersionForBundleName(anyString(), anyString())).thenThrow(ZincCatalog.DistributionNotFoundException.class);

        run();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void downloadsArchive() throws Exception {
        final int version = randomInt(1, 100);

        when(mZincCatalog.getVersionForBundleName(anyString(), anyString())).thenReturn(version);

        // run
        final ZincBundle result = run();

        // verify
        verify(mResultFuture).get();
        verifyDownloadArchiveJobCreation(version);
        checkResult(result);
    }

    private void checkResult(final ZincBundle result) {
        assertEquals(mResultPath, result.getPath());
        assertEquals(mBundleID, result.getBundleID());
    }

    private ZincCloneBundleJob initializeJob(final SourceURL sourceURL) {
        return new ZincCloneBundleJob(new ZincBundleCloneRequest(sourceURL, mBundleID, mDistribution, mFlavorName, mRepoFolder), mZincCatalogFuture, mFutureFactory);
    }

    private void verifyDownloadArchiveJobCreation(final int version) {
        verify(mFutureFactory).downloadArchive(eq(mArchiveURL), eq(mRepoFolder), anyString(), eq(false));
    }
}
