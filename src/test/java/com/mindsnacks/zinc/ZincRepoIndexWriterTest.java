package com.mindsnacks.zinc;

import com.mindsnacks.zinc.classes.ZincRepoIndexWriter;
import com.mindsnacks.zinc.utils.ZincBaseTest;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;

import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;

/**
 * Created by Tony Cosentini
 * Date: 11/14/13
 * Time: 4:55 PM
 */
public class ZincRepoIndexWriterTest extends ZincBaseTest {
    private ZincRepoIndexWriter mZincRepoIndexWriter;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private void setupZincRepoIndexWriter(File file) {
        mZincRepoIndexWriter = new ZincRepoIndexWriter(file, createGson());
    }

    @Test
    public void indexWriterCreatesFileAndReturnsAnIndexWhenFileDoesNotExist() throws Exception {
        setupZincRepoIndexWriter(temporaryFolder.newFolder());
        assertNotNull(mZincRepoIndexWriter.getIndex());
    }

    @Test
    public void indexWriterReplacesFIleAndReturnsAnIndexWhenGSONReturnsNullWhenParsingIndexJSON() throws Exception {
        setupZincRepoIndexWriter(temporaryFolder.newFolder());
        File indexFile = mZincRepoIndexWriter.getIndexFile();

        assertFalse(indexFile.exists());
        indexFile.createNewFile();
        assertTrue(indexFile.exists());

        assertNotNull(mZincRepoIndexWriter.getIndex());
    }
}
