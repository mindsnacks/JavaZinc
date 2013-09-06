package com.zinc.repo;

import com.zinc.classes.ZincCatalog;
import com.zinc.classes.ZincRepoIndex;
import com.zinc.classes.ZincRepoIndexWriter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.net.URL;
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
        final URL sourceURL = new URL("https://mindsnacks.com");
        final ZincCatalog catalog = createCatalog();

        when(mCatalogFuture.get()).thenReturn(catalog);
        when(mJobFactory.downloadCatalog(eq(sourceURL))).thenReturn(mCatalogFuture);

        // run
        mRepo.addSourceURL(sourceURL);

        // verify
        verify(mRepoIndex).addSourceURL(eq(sourceURL));
        verify(mJobFactory).downloadCatalog(eq(sourceURL));
    }

    @Test
    public void catalogDoesntGetDownloadedTwiceWhenAddingTheSameSourceTwice() throws Exception {
        final URL catalogURL = new URL("https://mindsnacks.com");

        // run
        mRepo.addSourceURL(catalogURL);
        mRepo.addSourceURL(catalogURL);

        // verify
        verify(mJobFactory, times(1)).downloadCatalog(eq(catalogURL));
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

//    @Test
//    public void trackingBundleClonesBundle() throws Exception {
//        final String bundleID = "com.mindsnacks.games.swell";
//        final String distribution = "master";
//
//        // run
//        mRepo.startTrackingBundle(bundleID, distribution);
//
//        // verify
//        verify(mJobFactory).cloneBundle(Matchers.<List<URL>>any(), eq(bundleID), eq(distribution), eq(mCatalogFuture), eq(rootFolder.getRoot()));
//    }
}
