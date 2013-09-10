package com.zinc.jobs;

import com.google.gson.Gson;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import com.zinc.utils.MockFactory;
import com.zinc.utils.ZincBaseTest;
import com.zinc.classes.data.ZincCatalog;
import com.zinc.classes.jobs.AbstractZincDownloadJob;
import com.zinc.classes.jobs.ZincDownloadObjectJob;
import com.zinc.classes.jobs.ZincRequestExecutor;

import java.net.URL;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * User: NachoSoto
 * Date: 9/3/13
 */
public class ZincDownloadObjectJobTest extends ZincBaseTest {
    @Mock
    private ZincRequestExecutor mRequestExecutor;

    private Gson mGson;

    @Before
    public void setUp() {
        mGson = createGson();
    }

    @Test
    public void testCall() throws Exception {
        final ZincCatalog catalog = MockFactory.createCatalog();
        final URL url = new URL("http://zinc2.mindsnacks.com.s3.amazonaws.com/com.mindsnacks.misc/index.json");

        final AbstractZincDownloadJob<ZincCatalog> mJob = new ZincDownloadObjectJob<ZincCatalog>(mRequestExecutor, url, mGson, ZincCatalog.class);

        // expectations
        final String catalogJSON = mGson.toJson(catalog);
        when(mRequestExecutor.get(url)).thenReturn(MockFactory.inputStreamWithString(catalogJSON));

        // run
        final ZincCatalog result = mJob.call();

        // verify
        assertEquals(catalog, result);
    }
}
