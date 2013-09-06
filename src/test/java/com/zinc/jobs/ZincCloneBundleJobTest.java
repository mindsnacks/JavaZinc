package com.zinc.jobs;

import com.zinc.classes.ZincBundle;
import com.zinc.classes.ZincCatalog;
import com.zinc.classes.ZincJobCreator;
import com.zinc.classes.jobs.AbstractZincDownloadJob;
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

    @Mock private Future<ZincCatalog> mZincCatalogFuture;
    @Mock private ZincCatalog mZincCatalog;
    @Mock private ZincJobCreator mJobCreator;
    @Mock private ZincJob<File> mDownloadArchiveJob;
    @Mock private File mRepoFolder;
    @Mock private File mResult;

    public ZincCloneBundleJobTest() throws MalformedURLException {
        mSourceURL = new URL("https://mindsnacks.com");
    }

    @Before
    public void setUp() throws Exception {
        mJob = initializeJob(Arrays.asList(mSourceURL));

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
        final int version = randomInt(1, 100);

        when(mDownloadArchiveJob.call()).thenReturn(mResult);
        when(mZincCatalog.getVersionForBundleID(anyString(), anyString())).thenReturn(version);

        // run
        final ZincBundle result = run();

        // verify
        verify(mDownloadArchiveJob).call();
        verifyDownloadArchiveJobCreation(mSourceURL, version);
        checkResult(result);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void triesDownloadingArchiveFromAllSources() throws Exception {
        final URL validSourceURL = new URL("http://zinc.mindsnacks.com");

        mJob = initializeJob(Arrays.asList(mSourceURL, validSourceURL));
        final ZincJob<File> downloadArchiveJob = mock(ZincJob.class);

        when(mJobCreator.downloadArchive(eq(validSourceURL), any(File.class), anyString())).thenReturn(downloadArchiveJob);

        when(mDownloadArchiveJob.call()).thenThrow(AbstractZincDownloadJob.DownloadFileError.class);
        when(downloadArchiveJob.call()).thenReturn(mResult);

        final int version = randomInt(1, 100);
        when(mZincCatalog.getVersionForBundleID(anyString(), anyString())).thenReturn(version);

        // run
        final ZincBundle result = run();

        // verify
        verify(mDownloadArchiveJob).call();
        verifyDownloadArchiveJobCreation(mSourceURL, version);
        verifyDownloadArchiveJobCreation(validSourceURL, version);
        checkResult(result);
    }

    private void checkResult(final ZincBundle result) {
        assertEquals(mResultPath, result.getPath());
        assertEquals(mBundleID, result.getBundleID());
    }

    private ZincCloneBundleJob initializeJob(final List<URL> sourceURLs) {
        return new ZincCloneBundleJob(sourceURLs, mBundleID, mDistribution, mZincCatalogFuture, mJobCreator, mRepoFolder);
    }

    private void verifyDownloadArchiveJobCreation(final URL validSourceURL, final int version) {
        verify(mJobCreator).downloadArchive(eq(validSourceURL), eq(mRepoFolder), eq("archives/" + mBundleID + "-" + version));
    }
}
