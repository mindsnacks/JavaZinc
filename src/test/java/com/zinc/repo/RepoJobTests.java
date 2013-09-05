package com.zinc.repo;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import com.zinc.classes.ZincCatalog;
import com.zinc.classes.ZincRepoIndex;
import com.zinc.classes.ZincRepoIndexWriter;
import com.zinc.classes.jobs.AbstractZincJob;

import java.net.URL;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static com.zinc.utils.MockFactory.createCatalogWithIdentifier;
import static com.zinc.utils.MockFactory.randomString;

/**
 * User: NachoSoto
 * Date: 9/3/13
 */
public class RepoJobTests extends RepoBaseTest {
    @Mock
    private AbstractZincJob<ZincCatalog> mZincCatalogDownloadJob;

    @Mock
    private ZincRepoIndex mRepoIndex;
    @Mock
    private ZincRepoIndexWriter mRepoIndexWriter;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        when(mRepoIndexWriter.getIndex()).thenReturn(mRepoIndex);
    }

    @Override
    protected ZincRepoIndexWriter newRepoIndexWriter() {
        return mRepoIndexWriter;
    }

    @Test
    public void catalogGetsDownloadedWhenAddingTheSource() throws Exception {
        final URL catalogURL = new URL("https://mindsnacks.com");
        final String catalogID = randomString();
        final ZincCatalog catalog = createCatalogWithIdentifier(catalogID);

        when(mZincCatalogDownloadJob.call()).thenReturn(catalog);
        when(mJobFactory.downloadCatalog(eq(catalogURL), eq(catalogID))).thenReturn(mZincCatalogDownloadJob);

        // run
        mRepo.addSourceURL(catalogURL, catalogID);

        // verify
        verify(mRepoIndex).addSourceURL(eq(catalogURL), eq(catalogID));
        verify(mJobFactory).downloadCatalog(eq(catalogURL), eq(catalogID));
    }

    @Test
    public void trackingBundleAddsItToTheIndex() throws Exception {
        final String bundleID = "com.mindsnacks.games.swell";
        final String distribution = "master";

        // run
        mRepo.startTrackingBundle(bundleID, distribution);

        // verify
        verify(mRepoIndex).trackBundle(eq(bundleID), eq(distribution));
    }

    @Test
    public void trackingBundleDownloadsTar() throws Exception {
        final String bundleID = "com.mindsnacks.games.swell";
        final String distribution = "master";

        // run
        mRepo.startTrackingBundle(bundleID, distribution);

        // verify
//        verify(mJobFactory).downloadArchive( bundleID)
    }
}
