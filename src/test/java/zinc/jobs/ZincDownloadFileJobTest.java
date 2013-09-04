package zinc.jobs;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import utils.ZincBaseTest;
import zinc.classes.jobs.ZincDownloadFileJob;
import zinc.classes.jobs.ZincRequestExecutor;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * User: NachoSoto
 * Date: 9/4/13
 */
public class ZincDownloadFileJobTest extends ZincBaseTest {
    @Mock
    private ZincRequestExecutor mRequestExecutor;

    @Rule
    public final TemporaryFolder rootFolder = new TemporaryFolder();

    private final URL mUrl;
    private ZincDownloadFileJob mJob;

    public ZincDownloadFileJobTest() throws MalformedURLException {
        mUrl = new URL("http://mindsnacks.com");
    }

    @Before
    public void setUp() throws Exception {
        mJob = new ZincDownloadFileJob(mRequestExecutor, mUrl, rootFolder.getRoot());
    }

    @Test
    public void testCreatesFolder() throws Exception {

    }
}
