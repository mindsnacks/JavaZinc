package com.mindsnacks.zinc.jobs;

import com.google.gson.Gson;
import com.mindsnacks.zinc.classes.fileutils.FileHelper;
import com.mindsnacks.zinc.classes.fileutils.HashUtil;
import com.mindsnacks.zinc.classes.jobs.ZincDownloadFileJob;
import com.mindsnacks.zinc.classes.jobs.ZincRequestExecutor;
import com.mindsnacks.zinc.utils.TestFactory;
import com.mindsnacks.zinc.utils.ZincBaseTest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static com.mindsnacks.zinc.utils.TestUtils.readFile;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * User: NachoSoto
 * Date: 9/4/13
 */
public class ZincDownloadFileJobTest extends ZincBaseTest {
    @Mock private ZincRequestExecutor mRequestExecutor;

    @Rule public final TemporaryFolder rootFolder = new TemporaryFolder();
    @Rule public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    private static final String EMPTY_FILE_HASH = "0a4d55a8d778e5022fab701977c5d840bbc486d0";
    private final URL mUrl = TestFactory.createURL("http://mindsnacks.com");
    private final String mFilename = "file";

    private ZincDownloadFileJob mJob;

    @Before
    public void setUp() throws Exception {
        initializeJob(true);
    }

    @Test
    public void createsFile() throws Exception {
        setURLContents("Hello World");

        assertTrue(run().exists());
    }

    @Test
    public void overridesFile() throws Exception {
        setURLContents("Hello World");
        createFolder();

        run();

        verifyFileIsDownloaded();
    }

    @Test
    public void doesntOverrideFile() throws Exception {
        initializeJob(false);

        createFolder();

        final File result = run();

        verify(mRequestExecutor, never()).get(any(URL.class));
        assertEquals(new File(rootFolder.getRoot(), mFilename), result);
    }

    @Test
    public void requestsURL() throws Exception {
        setURLContents("Hello World");

        run();

        verifyFileIsDownloaded();
    }

    @Test
    public void writesContentToFile() throws Exception {
        final String contents = "Hello World";

        setURLContents(contents);

        assertEquals(contents, readFile(run().getPath()));
    }

    private void initializeJob(final boolean override) throws FileNotFoundException {
        mJob = new ZincDownloadFileJob(mRequestExecutor, mUrl, rootFolder.getRoot(), temporaryFolder.getRoot(), mFilename, override, EMPTY_FILE_HASH, new FileHelper(new Gson(), new HashUtil()));
    }

    private File run() throws Exception {
        return mJob.call();
    }

    private void verifyFileIsDownloaded() {
        verify(mRequestExecutor).get(mUrl);
    }

    private void setURLContents(final String contents) {
        final InputStream reader = TestFactory.inputStreamWithString(contents);

        when(mRequestExecutor.get(mUrl)).thenReturn(reader);
    }

    private void createFolder() throws IOException {
        assert new File(rootFolder.getRoot(), mFilename).createNewFile();
    }
}
