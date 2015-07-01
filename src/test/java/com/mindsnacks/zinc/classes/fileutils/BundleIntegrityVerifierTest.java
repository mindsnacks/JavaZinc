package com.mindsnacks.zinc.classes.fileutils;

import com.mindsnacks.zinc.classes.data.ZincManifest;
import com.mindsnacks.zinc.utils.TestFactory;
import com.mindsnacks.zinc.utils.TestUtils;
import com.mindsnacks.zinc.utils.ZincBaseTest;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by Miguel Carranza on 6/30/15.
 */
public class BundleIntegrityVerifierTest extends ZincBaseTest {
    @Rule
    public final TemporaryFolder rootFolder = new TemporaryFolder();
    private final String mFlavorName = "peanut_butter";
    private final String mFilename1 = "file1.txt",
                         mFilename2 = "file2.txt",
                         mFilename3 = "file3.txt";
    private File mFile1, mFile2, mFile3;
    private String mContent1, mContent2, mContent3;
    private String mExpectedHash1, mExpectedHash2, mExpectedHash3;

    @Mock
    private ZincManifest mZincManifest;

    @Before
    public void setUp() throws Exception {
        mContent1 = TestFactory.randomString();
        mContent2 = TestFactory.randomString();
        mContent3 = TestFactory.randomString();

        mFile1 = TestUtils.createFile(rootFolder, mFilename1, mContent1);
        mFile2 = TestUtils.createFile(rootFolder, mFilename2, mContent2);
        mFile3 = TestUtils.createFile(rootFolder, mFilename3, mContent3);

        mExpectedHash1 = TestUtils.sha1HashString(mContent1);
        mExpectedHash2 = TestUtils.sha1HashString(mContent2);
        mExpectedHash3 = TestUtils.sha1HashString(mContent3);
    }

    @Test
    public void containsAllFilesAndHaveExpectedHash() {
        setUpManifest(createFilesWithFlavorsMap());

        assertTrue(BundleIntegrityVerifier.isLocalBundleValid(rootFolder.getRoot(),
                                                              mZincManifest,
                                                              mFlavorName));
    }

    @Test
    public void containsAllFilesButHashIsIncorrect() {
        mExpectedHash2 = "super wrong hash";
        setUpManifest(createFilesWithFlavorsMap());

        assertFalse(BundleIntegrityVerifier.isLocalBundleValid(rootFolder.getRoot(),
                mZincManifest,
                mFlavorName));
    }

    @Test
    public void doesNotContainAllFiles() {
        Map<String, ZincManifest.FileInfo> filesWithFlavorMap = createFilesWithFlavorsMap();
        filesWithFlavorMap.put("file_tocapelotas.txt", stubFileInfo("hash does not matter"));
        setUpManifest(filesWithFlavorMap);

        assertFalse(BundleIntegrityVerifier.isLocalBundleValid(rootFolder.getRoot(),
                mZincManifest,
                mFlavorName));
    }

    private ZincManifest.FileInfo stubFileInfo(String expectedHash) {
        ZincManifest.FileInfo res = mock(ZincManifest.FileInfo.class);
        when(res.getHash()).thenReturn(expectedHash);

        return res;
    }

    private void setUpManifest(Map<String, ZincManifest.FileInfo> files) {
        when(mZincManifest.getFilesWithFlavor(mFlavorName)).thenReturn(files);
    }

    private Map<String, ZincManifest.FileInfo> createFilesWithFlavorsMap() {
        Map<String, ZincManifest.FileInfo> res = new HashMap<String, ZincManifest.FileInfo>();
        res.put(mFilename1, stubFileInfo(mExpectedHash1));
        res.put(mFilename2, stubFileInfo(mExpectedHash2));
        res.put(mFilename3, stubFileInfo(mExpectedHash3));

        return res;
    }
}