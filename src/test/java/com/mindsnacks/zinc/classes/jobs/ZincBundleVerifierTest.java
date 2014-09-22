package com.mindsnacks.zinc.classes.jobs;

import com.mindsnacks.zinc.classes.data.ZincManifest;
import com.mindsnacks.zinc.classes.fileutils.HashUtil;
import com.mindsnacks.zinc.utils.ZincBaseTest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class ZincBundleVerifierTest extends ZincBaseTest {

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Mock
    private File mLocalBundleFolder;
    @Mock
    private ZincManifest mManifest;
    @Mock
    private HashUtil hashUtil;

    private String mFlavorName;
    private ZincBundleVerifier mVerifier;
    private File mTempFolderRoot;

    @Before
    public void setup() {
        mFlavorName = "test";
        mVerifier = new ZincBundleVerifier(hashUtil);
        mTempFolderRoot = temporaryFolder.getRoot();
    }

    @Test
    public void testBundleFolderDoesntExist() {
        when(mLocalBundleFolder.exists()).thenReturn(false);
        assertFalse(mVerifier.verify(mLocalBundleFolder, mManifest, mFlavorName));
    }

    @Test
    public void testEmptyLocalFolder() {
        when(mLocalBundleFolder.exists()).thenReturn(true);
        when(mLocalBundleFolder.listFiles()).thenReturn(new File[0]);
        assertFalse(mVerifier.verify(mLocalBundleFolder, mManifest, mFlavorName));
    }

    @Test
    public void testEmptyLocalFolderNullContents() {
        when(mLocalBundleFolder.exists()).thenReturn(true);
        when(mLocalBundleFolder.listFiles()).thenReturn(null);
        assertFalse(mVerifier.verify(mLocalBundleFolder, mManifest, mFlavorName));
    }

    @Test
    public void testBundleFileDoesntExist() throws IOException {
        setupLocalFolderExists();
        setupManifest("not_tester", "tester", false);

        assertFalse(mVerifier.verify(mTempFolderRoot, mManifest, mFlavorName));
    }

    @Test
    public void testBundleFileFailsHash() throws IOException {
        setupLocalFolderExists();
        setupManifest("tester", "tester", false);
        assertFalse(mVerifier.verify(mTempFolderRoot, mManifest, mFlavorName));
    }

    @Test
    public void testBundleFilePassesHash() throws IOException {
        setupLocalFolderExists();
        setupManifest("tester", "tester", true);
        assertTrue(mVerifier.verify(mTempFolderRoot, mManifest, mFlavorName));
    }

    private void setupLocalFolderExists() {
        when(mLocalBundleFolder.exists()).thenReturn(true);
        when(mLocalBundleFolder.listFiles()).thenReturn(new File[1]);
    }

    private void setupManifest(String fileInfoName, String fileName, boolean matchHash) throws IOException {
        String expectedHash = "expected_hash";
        String actualHash = matchHash ? expectedHash : "not_expected_hash";
        assertEquals(matchHash, expectedHash.equals(actualHash));

        Map<String, ZincManifest.FileInfo> fileInfoMap = new HashMap<String, ZincManifest.FileInfo>();
        fileInfoMap.put(fileInfoName, new ZincManifest.FileInfo(Collections.<String>emptySet(), actualHash, Collections.<String, Map<String, Integer>>emptyMap()));
        when(mManifest.getFilesWithFlavor(mFlavorName)).thenReturn(fileInfoMap);

        File target = new File(mTempFolderRoot, fileName);
        assertTrue(target.createNewFile());

        when(hashUtil.sha1HashString(any(InputStream.class))).thenReturn(expectedHash);
    }
}