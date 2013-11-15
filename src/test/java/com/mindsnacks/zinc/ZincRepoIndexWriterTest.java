package com.mindsnacks.zinc;

import com.mindsnacks.zinc.classes.ZincRepoIndexWriter;
import com.mindsnacks.zinc.utils.ZincBaseTest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;

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

    @Before
    public void setupZincRepoIndexWriter() throws Exception {
        mZincRepoIndexWriter = new ZincRepoIndexWriter(temporaryFolder.newFolder(), createGson());
    }

    @Test
    public void indexWriterCreatesFileAndReturnsAnIndexWhenFileDoesNotExist() throws Exception {
        assertNotNull(mZincRepoIndexWriter.getIndex());
    }

    @Test
    public void indexWriterReplacesFIleAndReturnsAnIndexWhenGSONReturnsNullWhenParsingIndexJSON() throws Exception {
        File emptyIndexFile = mZincRepoIndexWriter.getIndexFile();
        emptyIndexFile.createNewFile();

        assertNotNull(mZincRepoIndexWriter.getIndex());
    }
}
