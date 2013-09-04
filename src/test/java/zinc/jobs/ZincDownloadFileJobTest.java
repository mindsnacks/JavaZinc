package zinc.jobs;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import utils.MockFactory;
import utils.ZincBaseTest;
import zinc.classes.jobs.ZincDownloadFileJob;
import zinc.classes.jobs.ZincRequestExecutor;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static zinc.jobs.TestUtils.readFile;

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
        mJob = new ZincDownloadFileJob(mRequestExecutor, mUrl, rootFolder.getRoot(), mFilename);
    }

    private File run() {
        return mJob.call();
    }

    @Test
    public void createsFile() throws Exception {
        setURLContents("Hello World");

        final File file = run();

        assertTrue(file.exists());
    }

    @Test
    public void requestsURL() throws Exception {
        setURLContents("Hello World");

        run();

        verify(mRequestExecutor).get(mUrl);
    }

    @Test
    public void writesContentToFile() throws Exception {
        final String contents = "Hello World";

        setURLContents(contents);

        final File file = run();

        assertEquals(contents, readFile(file.getPath()));
    }

    private void setURLContents(final String contents) {
        final InputStream reader = MockFactory.inputStreamWithString(contents);

        when(mRequestExecutor.get(mUrl)).thenReturn(reader);
    }
}
