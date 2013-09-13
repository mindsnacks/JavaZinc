package com.zinc.jobs;

import com.zinc.classes.ZincFutureFactory;
import com.zinc.classes.data.*;
import com.zinc.classes.fileutils.FileHelper;
import com.zinc.classes.jobs.ZincUnarchiveBundleJob;
import com.zinc.utils.MockFactory;
import com.zinc.utils.ZincBaseTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

import static com.zinc.utils.MockFactory.randomInt;
import static com.zinc.utils.MockFactory.randomString;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * User: NachoSoto
 * Date: 9/10/13
 */
public class ZincUnarchiveBundleJobTest extends ZincBaseTest {

    private ZincBundleCloneRequest mBundleCloneRequest;

    private ZincUnarchiveBundleJob mJob;

    final private String mBundleName = "swell";
    final private String mCatalogID = "com.mindsnacks.games";
    final private BundleID mBundleID = new BundleID(mCatalogID, mBundleName);
    final private String mDistribution = "master";
    final private String mFlavorName = "retina";
    final private int mVersion = randomInt(5, 100);
    final private URL mSourceHost;

    @Mock private ZincBundle mBundle;
    private Future<ZincBundle> mBundleFuture;
    @Mock private ZincManifest mManifest;
    private Future<ZincManifest> mManifestFuture;
    @Mock private ZincFutureFactory mFutureFactory;
    @Mock private SourceURL mSourceURL;
    @Mock private File mRepoFolder;
    private final String mRepoFolderAbsolutePath = "/resources/zinc/";
    @Mock private FileHelper mFileHelper;

    public ZincUnarchiveBundleJobTest() throws MalformedURLException {
        mSourceHost = new URL("https://mindsnacks.com/");
    }

    @Before
    public void setUp() throws Exception {
        mBundleCloneRequest = new ZincBundleCloneRequest(mSourceURL, mBundleID, mDistribution, mFlavorName, mRepoFolder);
        mBundleFuture = MockFactory.createFutureWithResult(mBundle);
        mManifestFuture = MockFactory.createFutureWithResult(mManifest);

        when(mRepoFolder.getAbsolutePath()).thenReturn(mRepoFolderAbsolutePath);

        when(mBundle.getBundleID()).thenReturn(mBundleID);
        when(mBundle.getVersion()).thenReturn(mVersion);

        when(mSourceURL.getUrl()).thenReturn(mSourceHost);
        when(mSourceURL.getCatalogID()).thenReturn(mCatalogID);

        when(mFutureFactory.downloadManifest(eq(mSourceURL), eq(mBundleName), eq(mVersion))).thenReturn(mManifestFuture);

        mJob = new ZincUnarchiveBundleJob(mBundleFuture, mBundleCloneRequest, mFutureFactory, mFileHelper);
    }

    @Test
    public void downloadsTheBundle() throws Exception {
        run();

        verify(mBundleFuture).get();
    }

    @Test
    public void downloadsTheManifest() throws Exception {
        run();

        verify(mFutureFactory).downloadManifest(mSourceURL, mBundleName, mVersion);
    }

    @Test
    public void getsFilesForFlavor() throws Exception {
        run();

        verify(mManifest).getFilesWithFlavor(mFlavorName);
    }

    @Test
    public void unzipsAllFiles() throws Exception {
        final String filename1 = "file1.txt", filename2 = "file2.png";
        final String hash1 = randomString(), hash2 = randomString();
        final Map<String, ZincManifest.FileInfo> files = new HashMap<String, ZincManifest.FileInfo>();

        addFileInfo(files, filename1, hash1, hash1 + ".gz", true);
        addFileInfo(files, filename2, hash2, hash2, false);

        when(mManifest.getFilesWithFlavor(eq(mFlavorName))).thenReturn(files);

        final ZincBundle result = run();

        verify(mManifest).getFilesWithFlavor(mFlavorName);
        verify(mFileHelper, times(1)).unzipFile(eq(mBundle), eq(hash1 + ".gz"), any(ZincBundle.class), eq(filename1));
        verify(mFileHelper, times(1)).copyFile(eq(mBundle), eq(hash2), any(ZincBundle.class), eq(filename2));

        verify(mFileHelper, times(0)).unzipFile(eq(mBundle), anyString(), any(ZincBundle.class), eq(filename2));
        verify(mFileHelper, times(0)).copyFile(eq(mBundle), anyString(), any(ZincBundle.class), eq(filename1));

        verify(mFileHelper).removeFile(eq(mBundle));

        assertTrue(result.getAbsolutePath().startsWith(mRepoFolderAbsolutePath));
    }

    private ZincBundle run() throws Exception {
        return mJob.call();
    }

    private void addFileInfo(final Map<String, ZincManifest.FileInfo> files,
                             final String filename,
                             final String hash,
                             final String hashWithExtension,
                             final boolean isGzipped) {
        final ZincManifest.FileInfo info = mock(ZincManifest.FileInfo.class);
        when(info.getHash()).thenReturn(hash);
        when(info.getHashWithExtension()).thenReturn(hashWithExtension);
        when(info.isGzipped()).thenReturn(isGzipped);

        files.put(filename, info);
    }
}
