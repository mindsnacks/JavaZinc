package zinc.jobs;

import com.google.gson.Gson;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import utils.BaseTest;
import utils.MockFactory;
import zinc.classes.ZincCatalog;
import zinc.classes.jobs.ZincDownloadFileJob;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.net.URL;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * User: NachoSoto
 * Date: 9/3/13
 */
public class ZincDownloadFileJobTest extends BaseTest {
    @Mock
    private ZincDownloadFileJob.RequestFactory mRequestFactory;

    private Gson mGson;

    @Before
    public void setUp() {
        mGson = createGson();
    }

    @Test
    public void testCall() throws Exception {
        final ZincCatalog catalog = MockFactory.createCatalog();
        final URL url = new URL("http://zinc2.mindsnacks.com.s3.amazonaws.com/com.mindsnacks.misc/index.json");

        final ZincDownloadFileJob<ZincCatalog> mJob = new ZincDownloadFileJob<ZincCatalog>(mRequestFactory, url, mGson, ZincCatalog.class);

        // expectations
        final String catalogJSON = mGson.toJson(catalog);
        final InputStreamReader reader = new InputStreamReader(new ByteArrayInputStream(catalogJSON.getBytes()));
        when(mRequestFactory.get(url)).thenReturn(reader);

        // run
        final ZincCatalog result = mJob.call();

        // verify
        assertEquals(catalog, result);
    }
}
