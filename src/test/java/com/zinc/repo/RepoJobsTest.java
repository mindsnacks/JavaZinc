package com.zinc.repo;

import com.zinc.classes.ZincRepoIndexWriter;
import com.zinc.classes.data.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;

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
        mockDownloadCatalog();

        // run
        mRepo.addSourceURL(mSourceURL);

        // verify
        verify(mRepoIndex).addSourceURL(eq(mSourceURL));
        verify(mFutureFactory).downloadCatalog(eq(mSourceURL));
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

        mockDownloadCatalog();
        mockSourceURLForCatalog();
        mockGetTrackingInfo(bundleID, distribution);

        // run
        mRepo.startTrackingBundle(bundleID, distribution);

        // verify
        verifyCloneBundle(bundleID, distribution);
    }

    @Test
    public void bundlesAreAreClonedForAlreadyTrackedBundles() throws Exception {
        final BundleID bundleID = new BundleID(mCatalogID, "swell");
        final String distribution = "master";

        setUpIndexWithTrackedBundleID(bundleID, distribution);

        // run
        initializeRepo();

        // verify
        verifyCloneBundle(bundleID, distribution);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void getBundleWithIDCreatesThePromise() throws Exception {
        final BundleID bundleID = new BundleID(mCatalogID, "swell");
        final String distribution = "develop";

        final Future<ZincBundle> expectedResult = mock(Future.class);

        mockDownloadCatalog();
        mockSourceURLForCatalog();
        mockGetTrackingInfo(bundleID, distribution);
        mockCloneBundle(expectedResult);

        // run
        final Future<ZincBundle> result = mRepo.getBundle(bundleID);

        verifyCloneBundle(bundleID, distribution);
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

        verifyCloneBundle(bundleID, distribution);
        assertEquals(expectedResult, result);
    }

    private void setUpIndexWithTrackedBundleID(final BundleID bundleID,
                                               final String distribution) throws ZincRepoIndex.CatalogNotFoundException, ZincRepoIndex.BundleNotBeingTrackedException {
        mockDownloadCatalog();
        mockSourceURLForCatalog();
        mockGetTrackingInfo(bundleID, distribution);
        when(mRepoIndex.getTrackedBundleIDs()).thenReturn(new HashSet<BundleID>(Arrays.asList(bundleID)));
    }

    @SuppressWarnings("unchecked")
    private void mockCloneBundle(final Future<ZincBundle> expectedResult) {
        when(mFutureFactory.cloneBundle(any(ZincCloneBundleRequest.class), any(Future.class))).thenReturn(expectedResult);
    }

    private void mockGetTrackingInfo(final BundleID bundleID, final String distribution) throws ZincRepoIndex.BundleNotBeingTrackedException {
        when(mRepoIndex.getTrackingInfo(eq(bundleID))).thenReturn(new ZincRepoIndex.TrackingInfo(distribution));
    }

    private void mockSourceURLForCatalog() throws ZincRepoIndex.CatalogNotFoundException {
        when(mRepoIndex.getSourceURLForCatalog(eq(mCatalogID))).thenReturn(mSourceURL);
    }

    private void mockDownloadCatalog() {
        when(mFutureFactory.downloadCatalog(mSourceURL)).thenReturn(mCatalogFuture);
    }

    private void verifyCatalogIsDownloaded() {
        verify(mFutureFactory, times(1)).downloadCatalog(eq(mSourceURL));
    }

    private void verifyCloneBundle(final BundleID bundleID, final String distribution) {
        verify(mFutureFactory, times(1)).cloneBundle(argThat(new ArgumentMatcher<ZincCloneBundleRequest>() {
            @Override
            public boolean matches(final Object object) {
                final ZincCloneBundleRequest request = (ZincCloneBundleRequest) object;

                return (request.getSourceURL().equals(mSourceURL) &&
                        request.getBundleID().equals(bundleID) &&
                        request.getDistribution().equals(distribution) &&
                        request.getFlavorName().equals(mFlavorName) &&
                        request.getRepoFolder().equals(rootFolder.getRoot())
                );
            }
        }), eq(mCatalogFuture));
    }

    private void mockGetSources(List<SourceURL> sourceURLs) {
        when(mRepoIndex.getSources()).thenReturn(new HashSet<SourceURL>(sourceURLs));
    }
}
