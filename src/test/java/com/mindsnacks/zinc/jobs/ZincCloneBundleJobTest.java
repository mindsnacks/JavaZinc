package com.mindsnacks.zinc.jobs;

import com.mindsnacks.zinc.classes.ZincJobFactory;
import com.mindsnacks.zinc.classes.data.*;
import com.mindsnacks.zinc.classes.jobs.ZincBundleVerifier;
import com.mindsnacks.zinc.classes.jobs.ZincCloneBundleJob;
import com.mindsnacks.zinc.utils.TestFactory;
import com.mindsnacks.zinc.utils.TestUtils;
import com.mindsnacks.zinc.utils.ZincBaseTest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Matchers;
import org.mockito.Mock;

import java.io.File;
import java.io.IOException;
import java.net.URL;
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
    @Mock private Callable<File> mDownloadFileJob;
    @Mock private ZincBundle mResultBundle;
    @Mock private ZincBundle mDownloadedBundle;
    @Mock private ZincJobFactory mJobFactory;
    @Mock private Future<ZincCatalog> mZincCatalogFuture;
    @Mock private ZincCatalog mZincCatalog;
    @Mock private ZincManifest mZincManifest;
    @Mock private ZincManifest.FileInfo mFileWithFlavor;
    @Mock
    private ZincBundleVerifier mBundleVerifier;
    private URL mObjectURL;

    private final String mDistribution = "master";
    private final String mFlavorName = "retina";
    private final String mSingleFilename = "some-file";

    private int mVersion = 10;
    private final String mBundleName = "bundle";

    private File mRepoFolder;
    private File mDownloadedFile;

    private ZincCloneBundleRequest mRequest;
    private ZincCloneBundleJob job;

    @Before
    public void setUp() throws Exception {
        mRepoFolder = rootFolder.getRoot();
        mDownloadedFile = new File(rootFolder.getRoot(), "downloaded file");
        mObjectURL = new URL("https://www.nsa.gov");

        mRequest = new ZincCloneBundleRequest(mSourceURL, mBundleID, mDistribution, mFlavorName, mRepoFolder);
        job = new ZincCloneBundleJob(mRequest, mJobFactory, mZincCatalogFuture, mBundleVerifier);

        when(mJobFactory.downloadBundle(eq(mRequest), eq(mZincCatalogFuture))).thenReturn(mDownloadBundleJob);
        when(mJobFactory.downloadManifest(eq(mSourceURL), eq(mBundleName), eq(mVersion))).thenReturn(mZincManifestJob);
        when(mJobFactory.unarchiveBundle(any(ZincBundle.class), eq(mRequest), eq(mZincManifest))).thenReturn(mResultBundleJob);
        when(mJobFactory.downloadFile(eq(mObjectURL), any(File.class), eq(mSingleFilename), anyBoolean())).thenReturn(mDownloadFileJob);

        TestFactory.setCallableResult(mDownloadBundleJob, mDownloadedBundle);
        TestFactory.setCallableResult(mDownloadFileJob, mDownloadedFile);
        TestFactory.setCallableResult(mResultBundleJob, mResultBundle);
        TestFactory.setCallableResult(mZincManifestJob, mZincManifest);
        TestFactory.setFutureResult(mZincCatalogFuture, mZincCatalog);

        when(mBundleID.getBundleName()).thenReturn(mBundleName);
        when(mZincCatalog.getVersionForBundleName(mBundleName, mDistribution)).thenReturn(mVersion);
        when(mZincManifest.getFileWithFlavor(mFlavorName)).thenReturn(mFileWithFlavor);
        when(mZincManifest.getFilenameWithFlavor(mFlavorName)).thenReturn(mSingleFilename);
        when(mSourceURL.getObjectURL(mFileWithFlavor)).thenReturn(mObjectURL);
        setupBundleVerifier(true);


        setManifestContainsFiles(true);
        setManifestArchiveExists(true);
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
    public void bundleIDownloadedIfItAlreadyExistsButItsEmpty() throws Exception {
        createExpectedResultDirectory();

        run();

        verify(mJobFactory).downloadBundle(eq(mRequest), eq(mZincCatalogFuture));
    }

    @Test
    public void bundleIsNotDownloadedIfItAlreadyExists() throws Exception {
        setupBundleVerifier(false);
        createExpectedResultDirectoryWithFiles();

        run();

        verifyBundleIsNotUnarchived();
        verifyArchiveIsNotDownloaded();
    }

    @Test
    public void bundleIsReturnedIfItAlreadyExists() throws Exception {
        setupBundleVerifier(false);
        verifyResult(createExpectedResultDirectoryWithFiles(), run());
    }

    @Test
    public void bundleIsCorrectIfManifestContainsNoFiles() throws Exception {
        setManifestContainsFiles(false);

        verifyResult(
                new ZincBundle(expectedResultDirectory(),
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

        verifyArchiveIsNotDownloaded();
    }

    @Test
    public void archiveIsNotDownloadedIfItDoesntExist() throws Exception {
        setManifestArchiveExists(false);

        run();

        verifyArchiveIsNotDownloaded();
    }

    @Test
    public void fileIsDownloadedIfThereIsNoArchive() throws Exception {
        final File expectedResultDirectory = expectedResultDirectory();

        setManifestArchiveExists(false);

        run();

        verify(mJobFactory).downloadFile(eq(mObjectURL), eq(expectedResultDirectory), eq(mSingleFilename), eq(false));
    }

    @Test
    public void bundleIsCorrectIfOnlyOneFileIsDownloaded() throws Exception {
        setManifestArchiveExists(false);

        verifyResult(mDownloadedFile.getParentFile(), run());
    }

    private File createExpectedResultDirectory() throws IOException {
        final File file = expectedResultDirectory();

        assert file.getParentFile().exists() || file.mkdirs();
        assert file.exists() || file.createNewFile();

        assert file.exists();

        return file;
    }

    private File createExpectedResultDirectoryWithFiles() throws IOException {
        final File folder = createExpectedResultDirectory();

        TestUtils.createRandomFileInFolder(folder);

        return folder;
    }

    private File expectedResultDirectory() {
        return new File(
                mRepoFolder,
                PathHelper.getLocalBundleFolder(mBundleID, mVersion, mFlavorName));
    }

    private void setManifestContainsFiles(final boolean containsFiles) {
        when(mZincManifest.containsFiles(mFlavorName)).thenReturn(containsFiles);
    }

    private void setManifestArchiveExists(final boolean exists) {
        when(mZincManifest.archiveExists(mFlavorName)).thenReturn(exists);
    }

    private void verifyBundleIsNotUnarchived() {
        verify(mJobFactory, times(0)).unarchiveBundle(any(ZincBundle.class), any(ZincCloneBundleRequest.class), any(ZincManifest.class));
    }

    private void verifyArchiveIsNotDownloaded() {
        verify(mJobFactory, times(0)).downloadBundle(any(ZincCloneBundleRequest.class), anyCatalogFuture());
    }

    private void setupBundleVerifier(boolean download) {
        when(mBundleVerifier.shouldDownloadBundle(any(File.class), eq(mZincManifest), eq(mFlavorName))).thenReturn(download);
    }

    private void verifyResult(final File directory, final ZincBundle result) {
        assertEquals(directory.getAbsolutePath(), result.getAbsolutePath());
        assertEquals(mBundleID, result.getBundleID());
        assertEquals(mVersion, result.getVersion());
    }

    private static Future<ZincCatalog> anyCatalogFuture() {
        return Matchers.any();
    }

    private ZincBundle run() throws Exception {
        return job.call();
    }
}
