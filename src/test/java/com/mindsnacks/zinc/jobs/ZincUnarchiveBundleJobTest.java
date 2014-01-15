package com.mindsnacks.zinc.jobs;

import com.mindsnacks.zinc.classes.data.*;
import com.mindsnacks.zinc.classes.fileutils.FileHelper;
import com.mindsnacks.zinc.classes.jobs.ZincUnarchiveBundleJob;
import com.mindsnacks.zinc.utils.TestFactory;
import com.mindsnacks.zinc.utils.ZincBaseTest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static com.mindsnacks.zinc.utils.TestFactory.randomInt;
import static com.mindsnacks.zinc.utils.TestFactory.randomString;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * User: NachoSoto
 * Date: 9/10/13
 */
public class ZincUnarchiveBundleJobTest extends ZincBaseTest {

    private ZincCloneBundleRequest mBundleCloneRequest;

    private ZincUnarchiveBundleJob mJob;

    final private String mBundleName = "swell";
    final private String mCatalogID = "com.mindsnacks.games";
    final private BundleID mBundleID = new BundleID(mCatalogID, mBundleName);
    final private String mDistribution = "master";
    final private String mFlavorName = "retina";
    final private int mVersion = randomInt(5, 100);
    final private URL mSourceHost = TestFactory.createURL("https://mindsnacks.com/");

    @Rule public final TemporaryFolder rootFolder = new TemporaryFolder();

    @Mock private ZincBundle mBundle;
    @Mock private ZincManifest mManifest;
    @Mock private SourceURL mSourceURL;
    @Mock private FileHelper mFileHelper;

    private File mRepoFolder;

    @Before
    public void setUp() throws Exception {
        mRepoFolder = rootFolder.getRoot();

        mBundleCloneRequest = new ZincCloneBundleRequest(mSourceURL, mBundleID, mDistribution, mFlavorName, mRepoFolder);

        when(mBundle.getBundleID()).thenReturn(mBundleID);
        when(mBundle.getVersion()).thenReturn(mVersion);

        when(mSourceURL.getUrl()).thenReturn(mSourceHost);
        when(mSourceURL.getCatalogID()).thenReturn(mCatalogID);

        when(mFileHelper.moveFile(any(File.class), any(File.class))).thenReturn(true);

        mJob = new ZincUnarchiveBundleJob(mBundle, mBundleCloneRequest, mManifest, mFileHelper);
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

        assertTrue(result.getAbsolutePath().startsWith(mRepoFolder.getAbsolutePath()));
        assertTrue(result.getAbsolutePath().contains(expectedResultFolder()));
    }

    @Test
    public void removesDownloadedFolder() throws Exception {
        run();

        verify(mFileHelper).removeDirectory(eq(mBundle));
    }

    @Test
    public void movesResultBundle() throws Exception {
        run();

        ArgumentCaptor<File> temporaryFolder = ArgumentCaptor.forClass(File.class),
                             resultFolder = ArgumentCaptor.forClass(File.class);

        verify(mFileHelper).moveFile(temporaryFolder.capture(), resultFolder.capture());

        assertTrue(resultFolder.getValue().getAbsolutePath().contains(expectedResultFolder()));
        assertTrue(temporaryFolder.getValue().getAbsolutePath().contains(expectedTemporaryFolder()));
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

    private String expectedResultFolder() {
        return PathHelper.getLocalBundleFolder(mBundleID, mVersion, mFlavorName);
    }

    private String expectedTemporaryFolder() {
        return PathHelper.getLocalTemporaryBundleFolder(mBundleID, mVersion, mFlavorName);
    }
}
