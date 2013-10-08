package com.mindsnacks.zinc.jobs;

import com.mindsnacks.zinc.classes.ZincJobFactory;
import com.mindsnacks.zinc.classes.data.BundleID;
import com.mindsnacks.zinc.classes.data.SourceURL;
import com.mindsnacks.zinc.classes.data.ZincBundle;
import com.mindsnacks.zinc.classes.data.ZincCloneBundleRequest;
import com.mindsnacks.zinc.classes.jobs.ZincCloneBundleJob;
import com.mindsnacks.zinc.utils.TestFactory;
import com.mindsnacks.zinc.utils.ZincBaseTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.File;
import java.util.concurrent.Callable;

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
    @Mock private Callable<ZincBundle> mDownloadBundleJob;
    @Mock private Callable<ZincBundle> mResultBundleJob;
    @Mock private ZincBundle mResultBundle;
    @Mock private ZincBundle mDownloadedBundle;
    @Mock private ZincJobFactory mJobFactory;

    private final String mDistribution = "master";
    private final String mFlavorName = "retina";

    private ZincCloneBundleRequest mRequest;
    private ZincCloneBundleJob job;

    @Before
    public void setUp() throws Exception {
        mRequest = new ZincCloneBundleRequest(mSourceURL, mBundleID, mDistribution, mFlavorName, mRepoFolder);
        job = new ZincCloneBundleJob(mRequest, mJobFactory);

        when(mJobFactory.downloadBundle(eq(mRequest))).thenReturn(mDownloadBundleJob);
        when(mJobFactory.unarchiveBundle(any(ZincBundle.class), eq(mRequest))).thenReturn(mResultBundleJob);

        TestFactory.setCallableResult(mDownloadBundleJob, mDownloadedBundle);
        TestFactory.setCallableResult(mResultBundleJob, mResultBundle);
    }

    @Test
    public void bundleIsDownloaded() throws Exception {
        run();

        verify(mJobFactory).downloadBundle(eq(mRequest));
    }

    @Test
    public void bundleIsUnarchived() throws Exception {
        run();

        verify(mJobFactory).unarchiveBundle(eq(mDownloadedBundle), eq(mRequest));
    }

    @Test
    public void resultIsCorrect() throws Exception {
        assertEquals(mResultBundle, run());
    }

    private ZincBundle run() throws Exception {
        return job.call();
    }
}
