package com.mindsnacks.zinc.classes.jobs;

import com.mindsnacks.zinc.classes.data.ZincManifest;
import com.mindsnacks.zinc.utils.ZincBaseTest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class ZincBundleVerifierTest extends ZincBaseTest {

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Mock
    File mLocalBundleFolder;
    @Mock
    ZincManifest mManifest;

    private String mFlavorName;
    private ZincBundleVerifier mVerifier;
    private File mTempFolderRoot;

    @Before
    public void setup() {
        mFlavorName = "test";
        mVerifier = new ZincBundleVerifier();
        mTempFolderRoot = temporaryFolder.getRoot();
    }

    @Test
    public void testBundleFolderDoesntExist() {
        when(mLocalBundleFolder.exists()).thenReturn(false);
        assertTrue(mVerifier.shouldDownloadBundle(mLocalBundleFolder, mManifest, mFlavorName));
    }

    @Test
    public void testEmptyLocalFolder() {
        when(mLocalBundleFolder.exists()).thenReturn(true);
        when(mLocalBundleFolder.listFiles()).thenReturn(new File[0]);
        assertTrue(mVerifier.shouldDownloadBundle(mLocalBundleFolder, mManifest, mFlavorName));
    }

    @Test
    public void testEmptyLocalFolderNullContents() {
        when(mLocalBundleFolder.exists()).thenReturn(true);
        when(mLocalBundleFolder.listFiles()).thenReturn(null);
        assertTrue(mVerifier.shouldDownloadBundle(mLocalBundleFolder, mManifest, mFlavorName));
    }

    @Test
    public void testBundleFileDoesntExist() throws IOException {
        setupLocalFolderExists();
        setupManifest("not_tester", "NOT IMPORTANT", "tester", "test");

        assertTrue(mVerifier.shouldDownloadBundle(mTempFolderRoot, mManifest, mFlavorName));
    }

    @Test
    public void testBundleFileFailsHash() throws IOException {
        setupLocalFolderExists();
        setupManifest("tester", "NOT THE HASH", "tester", "test");
        assertTrue(mVerifier.shouldDownloadBundle(mTempFolderRoot, mManifest, mFlavorName));
    }

    @Test
    public void testBundleFilePassesHash() throws IOException {
        setupLocalFolderExists();
        setupManifest("tester", "4e1243bd22c66e76c2ba9eddc1f91394e57f9f83", "tester", "test\n");

        assertFalse(mVerifier.shouldDownloadBundle(mTempFolderRoot, mManifest, mFlavorName));
    }

    private void setupLocalFolderExists() {
        when(mLocalBundleFolder.exists()).thenReturn(true);
        when(mLocalBundleFolder.listFiles()).thenReturn(new File[1]);
    }

    private void setupManifest(String fileInfoName, String fileHash, String fileName, String fileContents) throws IOException {
        Map<String, ZincManifest.FileInfo> fileInfoMap = new HashMap<String, ZincManifest.FileInfo>();
        fileInfoMap.put(fileInfoName, new ZincManifest.FileInfo(Collections.<String>emptySet(), fileHash, Collections.<String, Map<String, Integer>>emptyMap()));
        when(mManifest.getFilesWithFlavor(mFlavorName)).thenReturn(fileInfoMap);

        File target = new File(mTempFolderRoot, fileName);
        assertTrue(target.createNewFile());

        FileWriter fileWriter = new FileWriter(target);
        fileWriter.write(fileContents);
        fileWriter.flush();
        fileWriter.close();
    }
}