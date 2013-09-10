package com.zinc.jobs;

import com.zinc.classes.ZincFutureFactory;
import com.zinc.classes.data.BundleID;
import com.zinc.classes.data.SourceURL;
import com.zinc.classes.data.ZincBundle;
import com.zinc.classes.data.ZincBundleCloneRequest;
import com.zinc.classes.jobs.ZincUnarchiveBundleJob;
import com.zinc.utils.ZincBaseTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.File;
import java.util.concurrent.Future;

import static org.mockito.Mockito.verify;

/**
 * User: NachoSoto
 * Date: 9/10/13
 */
public class ZincUnarchiveBundleJobTest extends ZincBaseTest {

    private final ZincBundleCloneRequest bundleCloneRequest;

    private ZincUnarchiveBundleJob mJob;

    final private String mBundleName = "swell";
    final private String mCatalogID = "com.mindsnacks.games";
    final private BundleID mBundleID = new BundleID(mCatalogID, mBundleName);
    final private String mDistribution = "master";
    final private String mFlavorName = "retina";

    @Mock private Future<ZincBundle> mBundle;
    @Mock private ZincFutureFactory mFutureFactory;
    @Mock private SourceURL mSourceURL;
    @Mock private File mRepoFolder;

    public ZincUnarchiveBundleJobTest() {
        bundleCloneRequest = new ZincBundleCloneRequest(mSourceURL, mBundleID, mDistribution, mFlavorName, mRepoFolder);
    }

    @Before
    public void setUp() throws Exception {
        mJob = new ZincUnarchiveBundleJob(mBundle, bundleCloneRequest, mFutureFactory);
    }

    @Test
    public void downloadsTheBundle() throws Exception {
        run();

        verify(mBundle).get();
    }

    private ZincBundle run() throws Exception {
        return mJob.call();
    }
}
