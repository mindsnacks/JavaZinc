package com.zinc.repo;

import com.zinc.classes.ZincRepoIndexWriter;
import com.zinc.classes.data.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Future;

import static com.zinc.utils.MockFactory.createCatalog;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * User: NachoSoto
 * Date: 9/3/13
 */
public class RepoJobsTest extends RepoBaseTest {
    @Mock private Future<ZincCatalog> mCatalogFuture;
    @Mock private ZincRepoIndex mRepoIndex;

    @Mock private ZincRepoIndexWriter mRepoIndexWriter;

    private final String mCatalogID;
    private final SourceURL mSourceURL;

    public RepoJobsTest() throws MalformedURLException {
        mCatalogID = "com.mindsnacks.lessons";
        mSourceURL = new SourceURL(new URL("https://mindsnacks.com"), mCatalogID);
    }

    @Override
    @Before
    public void setUp() throws Exception {
        when(mRepoIndexWriter.getIndex()).thenReturn(mRepoIndex);
        mockGetSources(new ArrayList<SourceURL>());

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
        mockGetSources(Arrays.asList(mSourceURL));

        // run
        initializeRepo();

        // verify
        verifyCatalogIsDownloaded();
    }

    @Test
    public void trackingBundleAddsItToTheIndex() throws Exception {
        final BundleID bundleID = new BundleID("com.mindsnacks.games.swell");
        final String distribution = "master";

        mockGetTrackingInfo(bundleID, distribution);

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
        mockSourceURLForCatalog();
        mockGetTrackingInfo(bundleID, distribution);

        // run
        mRepo.startTrackingBundle(bundleID, distribution);

        // verify
        verify(mJobFactory).cloneBundle(eq(mSourceURL), eq(bundleID), eq(distribution), eq(mCatalogFuture), eq(rootFolder.getRoot()));
    }

    @Test
    public void bundlesAreAreClonedForAlreadyTrackedBundles() throws Exception {
        final BundleID bundleID = new BundleID(mCatalogID, "swell");
        final String distribution = "master";

        setUpIndexWithTrackedBundleID(bundleID, distribution);

        // run
        initializeRepo();

        // verify
        verify(mJobFactory).cloneBundle(eq(mSourceURL), eq(bundleID), eq(distribution), eq(mCatalogFuture), eq(rootFolder.getRoot()));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void getBundleWithIDCreatesThePromise() throws Exception {
        final BundleID bundleID = new BundleID(mCatalogID, "swell");
        final String distribution = "develop";

        final Future<ZincBundle> expectedResult = mock(Future.class);

        mockGetTrackingInfo(bundleID, distribution);
        mockCloneBundle(expectedResult);

        // run
        final Future<ZincBundle> result = mRepo.getBundle(bundleID);

        verify(mJobFactory).cloneBundle(any(SourceURL.class), eq(bundleID), eq(distribution), any(Future.class), any(File.class));
        assertEquals(expectedResult, result);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void getBundleWithIDReturnsExistingPromise() throws Exception {
        final BundleID bundleID = new BundleID(mCatalogID, "swell");
        final String distribution = "develop";
        final Future<ZincBundle> expectedResult = mock(Future.class);

        setUpIndexWithTrackedBundleID(bundleID, distribution);
        mockCloneBundle(expectedResult);

        // run
        initializeRepo();

        // run
        final Future<ZincBundle> result = mRepo.getBundle(bundleID);

        verify(mJobFactory, times(1)).cloneBundle(any(SourceURL.class), eq(bundleID), eq(distribution), any(Future.class), any(File.class));
        assertEquals(expectedResult, result);
    }

    private void setUpIndexWithTrackedBundleID(final BundleID bundleID,
                                               final String distribution) throws ZincRepoIndex.CatalogNotFoundException {
        mockDownloadCatalog(mSourceURL);
        mockSourceURLForCatalog();
        mockGetTrackingInfo(bundleID, distribution);
        when(mRepoIndex.getTrackedBundleIDs()).thenReturn(new HashSet<BundleID>(Arrays.asList(bundleID)));
    }

    @SuppressWarnings("unchecked")
    private void mockCloneBundle(final Future<ZincBundle> expectedResult) {
        when(mJobFactory.cloneBundle(any(SourceURL.class), any(BundleID.class), anyString(), any(Future.class), any(File.class))).thenReturn(expectedResult);
    }

    private void mockGetTrackingInfo(final BundleID bundleID, final String distribution) {
        when(mRepoIndex.getTrackingInfo(eq(bundleID))).thenReturn(new ZincRepoIndex.TrackingInfo(distribution));
    }

    private void mockSourceURLForCatalog() throws ZincRepoIndex.CatalogNotFoundException {
        when(mRepoIndex.getSourceURLForCatalog(eq(mCatalogID))).thenReturn(mSourceURL);
    }

    private void mockDownloadCatalog(final SourceURL sourceURL) {
        when(mJobFactory.downloadCatalog(sourceURL)).thenReturn(mCatalogFuture);
    }

    private void verifyCatalogIsDownloaded() {
        verify(mJobFactory, times(1)).downloadCatalog(eq(mSourceURL));
    }

    private void mockGetSources(List<SourceURL> sourceURLs) {
        when(mRepoIndex.getSources()).thenReturn(new HashSet<SourceURL>(sourceURLs));
    }
}
