package com.mindsnacks.zinc.repo;

import com.google.common.util.concurrent.ListenableFuture;
import com.mindsnacks.zinc.classes.ZincRepoIndexWriter;
import com.mindsnacks.zinc.classes.data.*;
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
import java.util.Set;
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

    private final SourceURL mSourceURL;

    private final String mCatalogID = "com.mindsnacks.lessons";
    private final String mDistribution = "master";

    private final BundleID mBundleID = new BundleID(mCatalogID, "swell");
    private final BundleID mAnotherBundleID = new BundleID(mCatalogID, "another bundle");

    public ZincRepoJobsTest() throws MalformedURLException {
        mSourceURL = new SourceURL(new URL("https://mindsnacks.com"), mCatalogID);
    }

    @Override
    @Before
    public void setUp() throws Exception {
        when(mRepoIndexWriter.getIndex()).thenReturn(mRepoIndex);
        when(mRepoIndex.getSourceURLForCatalog(eq(mCatalogID))).thenReturn(mSourceURL);
        when(mRepoIndex.trackBundle(any(BundleID.class), anyString())).thenReturn(true);

        mockGetSources(new ArrayList<SourceURL>());

        super.setUp();
    }

    @Override
    protected ZincRepoIndexWriter newRepoIndexWriter() {
        return mRepoIndexWriter;
    }

    @Test
    public void repoIsNotStartedByDefault() throws Exception {
        verify(mQueue, times(0)).start();
    }

    @Test
    public void startRepoStartsTheQueue() throws Exception {
        mRepo.start();

        verify(mQueue).start();
    }

    @Test
    public void pauseRepo() throws Exception {
        mRepo.start();
        mRepo.pause();

        verify(mQueue).stop();
    }

    @Test
    public void prioritiesChanged() throws Exception {
        mRepo.recalculatePriorities();

        verify(mQueue).recalculatePriorities();
    }

    @Test
    public void trackingBundleAddsItToTheIndex() throws Exception {
        mockGetTrackingInfo(mBundleID, mDistribution);

        // run
        mRepo.startTrackingBundle(mBundleID, mDistribution);

        // verify
        verify(mRepoIndex).trackBundle(eq(mBundleID), eq(mDistribution));
    }

    @Test
    public void trackingBundleSavesTheIndex() throws Exception {
        mockGetTrackingInfo(mBundleID, mDistribution);

        // run
        mRepo.startTrackingBundle(mBundleID, mDistribution);

        // verify
        verifySaveIndex();
    }

    @Test
    public void trackingBundleClonesBundle() throws Exception {
        mockGetTrackingInfo(mBundleID, mDistribution);

        // run
        mRepo.startTrackingBundle(mBundleID, mDistribution);

        // verify
        verifyCloneBundle(mBundleID, mDistribution);
    }

    @Test
    public void trackingMultipleBundlesAddsThemToTheIndex() throws Exception {
        mockGetTrackingInfo(mBundleID, mDistribution);
        mockGetTrackingInfo(mAnotherBundleID, mDistribution);

        // run
        mRepo.startTrackingBundles(Arrays.asList(mBundleID, mAnotherBundleID), mDistribution);

        // verify
        verify(mRepoIndex).trackBundle(eq(mBundleID), eq(mDistribution));
        verify(mRepoIndex).trackBundle(eq(mAnotherBundleID), eq(mDistribution));
    }

    @Test
    public void trackingMultipleBundlesClonesThem() throws Exception {
        mockGetTrackingInfo(mBundleID, mDistribution);
        mockGetTrackingInfo(mAnotherBundleID, mDistribution);

        // run
        mRepo.startTrackingBundles(Arrays.asList(mBundleID, mAnotherBundleID), mDistribution);

        // verify
        verifyCloneBundle(mBundleID, mDistribution);
        verifyCloneBundle(mAnotherBundleID, mDistribution);
    }

    @Test
    public void trackingMultipleSavesTheIndexOnlyOnce() throws Exception {
        mockGetTrackingInfo(mBundleID, mDistribution);
        mockGetTrackingInfo(mAnotherBundleID, mDistribution);

        // run
        mRepo.startTrackingBundles(Arrays.asList(mBundleID, mAnotherBundleID), mDistribution);

        // verify
        verifySaveIndex();
    }

    @Test
    public void stopTrackingBundlesStopsTrackingThem() throws Exception {
        mockGetTrackingInfo(mBundleID, mDistribution);
        mockGetTrackingInfo(mAnotherBundleID, mDistribution);

        when(mRepoIndex.stopTrackingBundle(mBundleID, mDistribution)).thenReturn(true);

        // run
        mRepo.startTrackingBundles(Arrays.asList(mBundleID, mAnotherBundleID), mDistribution);

        // verify
        verifyCloneBundle(mBundleID, mDistribution);
        verifyCloneBundle(mAnotherBundleID, mDistribution);

        Set<BundleID> bundlesToStop = new HashSet<>();
        bundlesToStop.add(mBundleID);
        mRepo.stopTrackingBundles(bundlesToStop, mDistribution);

        verify(mRepoIndex, times(1)).stopTrackingBundle(eq(mBundleID), eq(mDistribution));
        verify(mQueue, times(1)).remove(any(ZincCloneBundleRequest.class));
        verify(mIndexWriter, times(2)).saveIndex();
    }

    @Test
    public void bundlesAreAreClonedForAlreadyTrackedBundles() throws Exception {
        setUpIndexWithTrackedBundleID(mBundleID, mDistribution);

        // run
        initializeRepo();

        // verify
        verifyCloneBundle(mBundleID, mDistribution);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void getBundleWithIDReturnsExistingPromise() throws Exception {
        final BundleID bundleID = new BundleID(mCatalogID, "swell");
        final String distribution = "develop";
        final ListenableFuture<ZincBundle> expectedResult = mock(ListenableFuture.class);

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
        mockGetTrackingInfo(bundleID, distribution);
        when(mRepoIndex.getTrackedBundleIDs()).thenReturn(new HashSet<BundleID>(Arrays.asList(bundleID)));
    }

    @SuppressWarnings("unchecked")
    private void mockCloneBundle(final BundleID bundleID, final ListenableFuture<ZincBundle> expectedResult) {
        when(mQueue.get(argThat(new ArgumentMatcher<ZincCloneBundleRequest>() {
            @Override
            public boolean matches(final Object o) {
                final ZincCloneBundleRequest request = (ZincCloneBundleRequest)o;

                return (request != null &&
                        request.getBundleID().equals(bundleID));
            }
        }))).thenReturn(expectedResult);
    }

    private void mockGetTrackingInfo(final BundleID bundleID,
                                     final String distribution) throws ZincRepoIndex.BundleNotBeingTrackedException {
        when(mRepoIndex.getTrackingInfo(eq(bundleID))).thenReturn(new ZincRepoIndex.TrackingInfo(distribution));
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

    private void verifySaveIndex() {
        verify(mIndexWriter, times(1)).saveIndex();
    }

    private void mockGetSources(List<SourceURL> sourceURLs) {
        when(mRepoIndex.getSources()).thenReturn(new HashSet<SourceURL>(sourceURLs));
    }
}
