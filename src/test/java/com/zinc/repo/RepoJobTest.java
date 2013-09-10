package com.zinc.repo;

import com.zinc.classes.ZincRepoIndexWriter;
import com.zinc.classes.data.BundleID;
import com.zinc.classes.data.SourceURL;
import com.zinc.classes.data.ZincCatalog;
import com.zinc.classes.data.ZincRepoIndex;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.Future;

import static com.zinc.utils.MockFactory.createCatalog;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * User: NachoSoto
 * Date: 9/3/13
 */
public class RepoJobTest extends RepoBaseTest {
    @Mock private Future<ZincCatalog> mCatalogFuture;
    @Mock private ZincRepoIndex mRepoIndex;

    @Mock private ZincRepoIndexWriter mRepoIndexWriter;

    private final String mCatalogID;
    private final SourceURL mSourceURL;

    public RepoJobTest() throws MalformedURLException {
        mCatalogID = "com.mindsnacks.lessons";
        mSourceURL = new SourceURL(new URL("https://mindsnacks.com"), mCatalogID);
    }

    @Override
    @Before
    public void setUp() throws Exception {
        when(mRepoIndexWriter.getIndex()).thenReturn(mRepoIndex);
        when(mRepoIndex.getSources()).thenReturn(new HashSet<SourceURL>());

        super.setUp();
    }

    @Override
    protected ZincRepoIndexWriter newRepoIndexWriter() {
        return mRepoIndexWriter;
    }

    @Test
    public void catalogGetsDownloadedWhenAddingTheSource() throws Exception {
        final ZincCatalog catalog = createCatalog();

        when(mCatalogFuture.get()).thenReturn(catalog);
        mockDownloadCatalog(eq(mSourceURL));

        // run
        mRepo.addSourceURL(mSourceURL);

        // verify
        verify(mRepoIndex).addSourceURL(eq(mSourceURL));
        verify(mJobFactory).downloadCatalog(eq(mSourceURL));
    }

    @Test
    public void catalogDoesntGetDownloadedTwiceWhenAddingTheSameSourceTwice() throws Exception {
        // run
        mRepo.addSourceURL(mSourceURL);
        mRepo.addSourceURL(mSourceURL);

        // verify
        verifyCatalogIsDownloaded();
    }

    @Test
    public void catalogsAreDownloadedForExistingSourceURLs() throws Exception {
        when(mRepoIndex.getSources()).thenReturn(new HashSet<SourceURL>(Arrays.asList(mSourceURL)));

        // run
        initializeRepo();

        // verify
        verifyCatalogIsDownloaded();
    }

    @Test
    public void trackingBundleAddsItToTheIndex() throws Exception {
        final BundleID bundleID = new BundleID("com.mindsnacks.games.swell");
        final String distribution = "master";

        // run
        mRepo.startTrackingBundle(bundleID, distribution);

        // verify
        verify(mRepoIndex).trackBundle(eq(bundleID), eq(distribution));
    }

    @Test
    public void trackingBundleClonesBundle() throws Exception {
        final BundleID bundleID = new BundleID(mCatalogID, "swell");
        final String distribution = "master";

        mockDownloadCatalog(mSourceURL);
        when(mRepoIndex.getSourceURLForCatalog(eq(mCatalogID))).thenReturn(mSourceURL);

        // run
        mRepo.startTrackingBundle(bundleID, distribution);

        // verify
        verify(mJobFactory).cloneBundle(eq(mSourceURL), eq(bundleID), eq(distribution), eq(mCatalogFuture), eq(rootFolder.getRoot()));
    }

    private void mockDownloadCatalog(final SourceURL sourceURL) {
        when(mJobFactory.downloadCatalog(sourceURL)).thenReturn(mCatalogFuture);
    }

    private void verifyCatalogIsDownloaded() {
        verify(mJobFactory, times(1)).downloadCatalog(eq(mSourceURL));
    }
}
