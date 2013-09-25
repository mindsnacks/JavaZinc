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

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * User: NachoSoto
 * Date: 9/3/13
 */
public class ZincRepoJobsTest extends ZincRepoBaseTest {
    @Mock private Future<ZincCatalog> mCatalogFuture;
    @Mock private ZincRepoIndex mRepoIndex;

    @Mock private ZincRepoIndexWriter mRepoIndexWriter;

    private final String mCatalogID;
    private final SourceURL mSourceURL;

    public ZincRepoJobsTest() throws MalformedURLException {
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
    public void getBundleWithIDReturnsExistingPromise() throws Exception {
        final BundleID bundleID = new BundleID(mCatalogID, "swell");
        final String distribution = "develop";
        final Future<ZincBundle> expectedResult = mock(Future.class);

        setUpIndexWithTrackedBundleID(bundleID, distribution);
        mockCloneBundle(bundleID, expectedResult);

        // run
        initializeRepo();

        // run
        final Future<ZincBundle> result = mRepo.getBundle(bundleID);

        verifyCloneBundle(bundleID, distribution);
        assertEquals(expectedResult, result);
    }

    private void setUpIndexWithTrackedBundleID(final BundleID bundleID,
                                               final String distribution) throws ZincRepoIndex.CatalogNotFoundException, ZincRepoIndex.BundleNotBeingTrackedException {
        mockSourceURLForCatalog();
        mockGetTrackingInfo(bundleID, distribution);
        when(mRepoIndex.getTrackedBundleIDs()).thenReturn(new HashSet<BundleID>(Arrays.asList(bundleID)));
    }

    @SuppressWarnings("unchecked")
    private void mockCloneBundle(final BundleID bundleID, final Future<ZincBundle> expectedResult) {
        when(mQueue.get(argThat(new ArgumentMatcher<ZincCloneBundleRequest>() {
            @Override
            public boolean matches(final Object o) {
                final ZincCloneBundleRequest request = (ZincCloneBundleRequest)o;

                return (request != null &&
                        request.getBundleID().equals(bundleID));
            }
        }))).thenReturn(expectedResult);
    }

    private void mockGetTrackingInfo(final BundleID bundleID, final String distribution) throws ZincRepoIndex.BundleNotBeingTrackedException {
        when(mRepoIndex.getTrackingInfo(eq(bundleID))).thenReturn(new ZincRepoIndex.TrackingInfo(distribution));
    }

    private void mockSourceURLForCatalog() throws ZincRepoIndex.CatalogNotFoundException {
        when(mRepoIndex.getSourceURLForCatalog(eq(mCatalogID))).thenReturn(mSourceURL);
    }

    private void verifyCloneBundle(final BundleID bundleID, final String distribution) {
        verify(mQueue, times(1)).add(argThat(new ArgumentMatcher<ZincCloneBundleRequest>() {
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
        }));
    }

    private void mockGetSources(List<SourceURL> sourceURLs) {
        when(mRepoIndex.getSources()).thenReturn(new HashSet<SourceURL>(sourceURLs));
    }
}
