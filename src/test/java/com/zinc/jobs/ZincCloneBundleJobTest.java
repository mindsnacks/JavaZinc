package com.zinc.jobs;

import com.zinc.classes.ZincBundle;
import com.zinc.classes.ZincCatalog;
import com.zinc.classes.ZincJobCreator;
import com.zinc.classes.jobs.ZincCloneBundleJob;
import com.zinc.classes.jobs.ZincJob;
import com.zinc.utils.ZincBaseTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.concurrent.Future;

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

    @Mock private Future<ZincCatalog> mZincCatalogFuture;
    @Mock private ZincCatalog mZincCatalog;
    @Mock private ZincJobCreator mJobCreator;
    @Mock private File mRepoFolder;
    @Mock private ZincJob<File> mDownloadArchiveJob;
    @Mock private File mResult;

    public ZincCloneBundleJobTest() throws MalformedURLException {
        mSourceURL = new URL("https://mindsnacks.com");
    }

    @Before
    public void setUp() throws Exception {
        mJob = new ZincCloneBundleJob(Arrays.asList(mSourceURL), mBundleID, mDistribution, mZincCatalogFuture, mJobCreator, mRepoFolder);

        when(mZincCatalogFuture.get()).thenReturn(mZincCatalog);
        when(mJobCreator.downloadArchive(any(URL.class), any(File.class), anyString())).thenReturn(mDownloadArchiveJob);
        when(mDownloadArchiveJob.call()).thenReturn(mResult);
        when(mResult.getPath()).thenReturn(mResultPath);
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
        when(mDownloadArchiveJob.call()).thenReturn(mResult);

        final ZincBundle result = run();

        verify(mDownloadArchiveJob).call();
        verify(mJobCreator).downloadArchive(eq(mSourceURL), eq(mRepoFolder), eq("archives/" + mBundleID));
        assertEquals(mResultPath, result.getPath());
        assertEquals(mBundleID, result.getBundleID());
    }
}
