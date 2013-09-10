package com.zinc.jobs;

import com.zinc.classes.jobs.ZincDownloadFileJob;
import com.zinc.classes.jobs.ZincRequestExecutor;
import com.zinc.utils.MockFactory;
import com.zinc.utils.ZincBaseTest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import static com.zinc.utils.TestUtils.readFile;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * User: NachoSoto
 * Date: 9/4/13
 */
public class ZincDownloadFileJobTest extends ZincBaseTest {
    @Mock
    private ZincRequestExecutor mRequestExecutor;

    @Rule
    public final TemporaryFolder rootFolder = new TemporaryFolder();

    private final String mFilename;

    private final URL mUrl;
    private ZincDownloadFileJob mJob;

    public ZincDownloadFileJobTest() throws MalformedURLException {
        mUrl = new URL("http://mindsnacks.com");
        mFilename = "file";
    }

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

    private void initializeJob(final boolean override) {
        mJob = new ZincDownloadFileJob(mRequestExecutor, mUrl, rootFolder.getRoot(), mFilename, override);
    }

    private File run() {
        return mJob.call();
    }

    private void verifyFileIsDownloaded() {
        verify(mRequestExecutor).get(mUrl);
    }

    private void setURLContents(final String contents) {
        final InputStream reader = MockFactory.inputStreamWithString(contents);

        when(mRequestExecutor.get(mUrl)).thenReturn(reader);
    }

    private void createFolder() throws IOException {
        new File(rootFolder.getRoot(), mFilename).createNewFile();
    }
}
