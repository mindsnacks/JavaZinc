package com.mindsnacks.zinc.jobs;

import com.mindsnacks.zinc.classes.ZincJobFactory;
import com.mindsnacks.zinc.classes.data.*;
import com.mindsnacks.zinc.classes.jobs.ZincCloneBundleJob;
import com.mindsnacks.zinc.utils.TestFactory;
import com.mindsnacks.zinc.utils.ZincBaseTest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Matchers;
import org.mockito.Mock;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * User: NachoSoto
 * Date: 9/19/13
 */
public class ZincCloneBundleJobTest extends ZincBaseTest {
    @Rule public final TemporaryFolder rootFolder = new TemporaryFolder();

    @Mock private SourceURL mSourceURL;
    @Mock private BundleID mBundleID;
    @Mock private Callable<ZincBundle> mDownloadBundleJob;
    @Mock private Callable<ZincBundle> mResultBundleJob;
    @Mock private Callable<ZincManifest> mZincManifestJob;
    @Mock private ZincBundle mResultBundle;
    @Mock private ZincBundle mDownloadedBundle;
    @Mock private ZincJobFactory mJobFactory;
    @Mock private Future<ZincCatalog> mZincCatalogFuture;
    @Mock private ZincCatalog mZincCatalog;
    @Mock private ZincManifest mZincManifest;

    private final String mDistribution = "master";
    private final String mFlavorName = "retina";
    private int mVersion = 10;

    private final String mBundleName = "bundle";
    private File mRepoFolder;

    private ZincCloneBundleRequest mRequest;
    private ZincCloneBundleJob job;

    @Before
    public void setUp() throws Exception {
        mRepoFolder = rootFolder.getRoot();

        mRequest = new ZincCloneBundleRequest(mSourceURL, mBundleID, mDistribution, mFlavorName, mRepoFolder);
        job = new ZincCloneBundleJob(mRequest, mJobFactory, mZincCatalogFuture);

        when(mJobFactory.downloadBundle(eq(mRequest), eq(mZincCatalogFuture))).thenReturn(mDownloadBundleJob);
        when(mJobFactory.downloadManifest(eq(mSourceURL), eq(mBundleName), eq(mVersion))).thenReturn(mZincManifestJob);
        when(mJobFactory.unarchiveBundle(any(ZincBundle.class), eq(mRequest), eq(mZincManifest))).thenReturn(mResultBundleJob);

        TestFactory.setCallableResult(mDownloadBundleJob, mDownloadedBundle);
        TestFactory.setCallableResult(mResultBundleJob, mResultBundle);
        TestFactory.setCallableResult(mZincManifestJob, mZincManifest);
        TestFactory.setFutureResult(mZincCatalogFuture, mZincCatalog);

        when(mBundleID.getBundleName()).thenReturn(mBundleName);
        when(mZincCatalog.getVersionForBundleName(mBundleName, mDistribution)).thenReturn(mVersion);

        setManifestContainsFiles(true);
    }

    @Test
    public void bundleIsDownloaded() throws Exception {
        run();

        verify(mJobFactory).downloadBundle(eq(mRequest), eq(mZincCatalogFuture));
    }

    @Test
    public void manifestIsDownloaded() throws Exception {
        run();

        verify(mJobFactory).downloadManifest(eq(mSourceURL), eq(mBundleName), eq(mVersion));
    }

    @Test
    public void bundleIsUnarchived() throws Exception {
        run();

        verify(mJobFactory).unarchiveBundle(eq(mDownloadedBundle), eq(mRequest), eq(mZincManifest));
    }

    @Test
    public void resultIsCorrect() throws Exception {
        assertEquals(mResultBundle, run());
    }

    @Test
    public void bundleIsNotDownloadedifItAlreadyExists() throws Exception {
        createDirectory();

        run();

        verify(mJobFactory, times(0)).unarchiveBundle(any(ZincBundle.class), any(ZincCloneBundleRequest.class), any(ZincManifest.class));
        verify(mJobFactory, times(0)).downloadBundle(any(ZincCloneBundleRequest.class), anyCatalogFuture());
    }

    @Test
    public void bundleIsReturnedIfItAlreadyExists() throws Exception {
        verifyResult(createDirectory(), run());
    }

    @Test
    public void bundleIsCorrectIfManifestContainsNoFiles() throws Exception {
        setManifestContainsFiles(false);

        verifyResult(
                new ZincBundle(new File(mRepoFolder,
                        PathHelper.getLocalBundleFolder(mBundleID, mVersion, mFlavorName)),
                        mBundleID,
                        mVersion),
                run());
    }
    @Test
    public void bundleFolderIsEmptyIfManifestContainsNoFiles() throws Exception {
        setManifestContainsFiles(false);

        final ZincBundle result = run();

        assertTrue(result.exists());
        assertEquals(0, result.listFiles().length);
    }

    @Test
    public void nothingIsDownloadedIfManifestContainsNoFiles() throws Exception {
        setManifestContainsFiles(false);

        run();
        verify(mJobFactory, times(0)).downloadBundle(any(ZincCloneBundleRequest.class), anyCatalogFuture());
    }

    private File createDirectory() throws IOException {
        final File file = new File(
                mRepoFolder,
                PathHelper.getLocalBundleFolder(mBundleID, mVersion, mFlavorName));

        file.mkdirs();
        file.createNewFile();

        assert file.exists();

        return file;
    }

    private void setManifestContainsFiles(final boolean containsFiles) {
        when(mZincManifest.containsFiles(mFlavorName)).thenReturn(containsFiles);
    }

    private void verifyResult(final File directory, final ZincBundle result) {
        assertEquals(directory.getAbsolutePath(), result.getAbsolutePath());
        assertEquals(mBundleID, result.getBundleID());
        assertEquals(mVersion, result.getVersion());
    }

    private static Future<ZincCatalog> anyCatalogFuture() {
        return Matchers.<Future<ZincCatalog>>any();
    }

    private ZincBundle run() throws Exception {
        return job.call();
    }
}
