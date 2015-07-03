package com.mindsnacks.zinc.data;

import com.mindsnacks.zinc.classes.data.BundleID;
import com.mindsnacks.zinc.classes.data.PathHelper;
import com.mindsnacks.zinc.classes.data.ZincUntrackedBundlesCleaner;
import com.mindsnacks.zinc.classes.fileutils.FileHelper;
import com.mindsnacks.zinc.utils.TestUtils;
import com.mindsnacks.zinc.utils.ZincBaseTest;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;

import java.io.File;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Created by Miguel Carranza on 7/2/15.
 */
public class ZincUntrackedBundlesCleanerTest extends ZincBaseTest {
    @Rule public final TemporaryFolder rootFolder = new TemporaryFolder();
    @Mock private FileHelper mFileHelper;
    private File mManifestsFolder;
    private final int mVersion = 1245;

    private final BundleID mBundleID = new BundleID("com.wonder.content.sat-public_speaking");
    private File mBundle1, mBundle2, mBundle3, mManifest1, mManifest2, mManifest3;
    private ZincUntrackedBundlesCleaner mBundlesCleaner;

    @Before
    public void setUp() {
        mBundlesCleaner = new ZincUntrackedBundlesCleaner(mFileHelper);
    }

    @Test
    public void cleanUntrackedBundlesCleansBundles() throws Exception {
        createZincBundles();

        assertTrue(mBundle1.exists());
        assertTrue(mBundle2.exists());
        assertTrue(mBundle3.exists());

        mBundlesCleaner.cleanUntrackedBundles(rootFolder.getRoot(),
                                              mBundleID,
                                              mVersion);

        verify(mFileHelper, times(1)).removeDirectory(eq(mBundle1));
        verify(mFileHelper, times(1)).removeDirectory(eq(mBundle2));
        verify(mFileHelper, times(0)).removeDirectory(eq(mBundle3));
    }

    @Test
    public void cleanUntrackedBundlesCleansManifests() throws Exception {
        createZincManifests();

        assertTrue(mManifest1.exists());
        assertTrue(mManifest2.exists());
        assertTrue(mManifest3.exists());

        mBundlesCleaner.cleanUntrackedBundles(rootFolder.getRoot(),
                mBundleID,
                1245);

        verify(mFileHelper, times(1)).removeFile(eq(mManifest1));
        verify(mFileHelper, times(1)).removeFile(eq(mManifest2));
        verify(mFileHelper, times(0)).removeFile(eq(mManifest3));
    }

    @Test
    public void cleanUntrackedBundlesDoesNotThrowWhenFoldersDoNotExist() {
        mBundlesCleaner.cleanUntrackedBundles(rootFolder.getRoot(),
                                              mBundleID,
                                              mVersion);
    }

    private void createZincBundles() throws Exception {
        String  localBundleFolder1 = PathHelper.getLocalBundleFolder(mBundleID, 23, "3xiOS"),
                localBundleFolder2 = PathHelper.getLocalBundleFolder(mBundleID, 1323, "3xAndroid"),
                localBundleFolder3 = PathHelper.getLocalBundleFolder(new BundleID("com.wonder.content.sat-public_speaking2"), 23, "3xiOS");

        mBundle1 = new File(rootFolder.getRoot(), localBundleFolder1);
        mBundle2 = new File(rootFolder.getRoot(), localBundleFolder2);
        mBundle3 = new File(rootFolder.getRoot(), localBundleFolder3);

        mBundle1.mkdirs();
        mBundle2.mkdirs();
        mBundle3.mkdirs();
    }

    private void createZincManifests() throws Exception {
        mManifestsFolder = new File(rootFolder.getRoot(),
                                    PathHelper.getManifestsFolder(mBundleID.getCatalogID()));
        mManifestsFolder.mkdirs();

        mManifest1 = new File(mManifestsFolder,
                              PathHelper.getManifestID(mBundleID.getBundleName(), mVersion + 1) + ".json");
        mManifest2 = new File(mManifestsFolder,
                              PathHelper.getManifestID(mBundleID.getBundleName(), mVersion + 2) + ".json");
        mManifest3 = new File(mManifestsFolder,
                              PathHelper.getManifestID(mBundleID.getBundleName(), mVersion)  + ".json");

        TestUtils.writeToFile(mManifest1,
                              "whatever");
        TestUtils.writeToFile(mManifest2,
                              "whatever");
        TestUtils.writeToFile(mManifest3,
                              "whatever");
    }
}
