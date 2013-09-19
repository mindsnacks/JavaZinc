package com.zinc.jobs;

import com.zinc.classes.ZincFutureFactory;
import com.zinc.classes.data.*;
import com.zinc.classes.jobs.ZincCloneBundleJob;
import com.zinc.utils.MockFactory;
import com.zinc.utils.ZincBaseTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.File;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * User: NachoSoto
 * Date: 9/19/13
 */
public class ZincCloneBundleJobTest extends ZincBaseTest {
    @Mock private SourceURL mSourceURL;
    @Mock private BundleID mBundleID;
    @Mock private File mRepoFolder;
    @Mock private Future<ZincCatalog> mCatalogFuture;
    @Mock private Future<ZincBundle> mDownloadedBundleFuture;
    @Mock private Future<ZincBundle> mResultBundleFuture;
    @Mock private ZincBundle mResultBundle;
    @Mock private ZincFutureFactory mFutureFactory;

    private final String mDistribution = "master";

    private final String mFlavorName = "retina";
    private ZincCloneBundleRequest mRequest;
    private ZincCloneBundleJob job;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        mRequest = new ZincCloneBundleRequest(mSourceURL, mBundleID, mDistribution, mFlavorName, mRepoFolder);
        job = new ZincCloneBundleJob(mRequest, mCatalogFuture, mFutureFactory);

        when(mFutureFactory.unarchiveBundle(any(Future.class), eq(mRequest))).thenReturn(mResultBundleFuture);
        MockFactory.setFutureResult(mResultBundleFuture, mResultBundle);
    }

    @Test
    public void bundleIsDownloaded() throws Exception {
        run();

        verify(mFutureFactory).downloadBundle(eq(mRequest), eq(mCatalogFuture));
    }

    @Test
    public void bundleIsUnarchived() throws Exception {
        when(mFutureFactory.downloadBundle(mRequest, mCatalogFuture)).thenReturn(mDownloadedBundleFuture);

        run();

        verify(mFutureFactory).unarchiveBundle(eq(mDownloadedBundleFuture), eq(mRequest));
    }

    @Test
    public void resultIsCorrect() throws Exception {
        final ZincBundle result = run();

        assertEquals(mResultBundle, result);
    }

    private ZincBundle run() throws Exception {
        return job.call();
    }

}
