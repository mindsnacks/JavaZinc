package com.zinc.classes.jobs;

import com.zinc.classes.ZincBundle;
import com.zinc.classes.ZincCatalog;
import com.zinc.utils.ZincBaseTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.concurrent.Future;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * User: NachoSoto
 * Date: 9/5/13
 */
public class ZincCloneBundleJobTest extends ZincBaseTest {
    private ZincCloneBundleJob mJob;

    final private String mBundleID = "com.mindsnacks.games.swell";
    final private String mDistribution = "master";

    @Mock
    private Future<ZincCatalog> mZincCatalogFuture;
    @Mock
    private ZincCatalog mZincCatalog;
    @Mock
    private ZincDownloadArchiveJob mDownloadArchiveJob;;

    @Before
    public void setUp() throws Exception {
        mJob = new ZincCloneBundleJob(mBundleID, mDistribution, mZincCatalogFuture, mDownloadArchiveJob);

        when(mZincCatalogFuture.get()).thenReturn(mZincCatalog);
    }

    private ZincBundle run() throws Exception {
        return mJob.call();
    }

    @Test
    public void getsCatalog() throws Exception {
        run();

        verify(mZincCatalogFuture).get();
    }

    @Test
    public void getsDistributionVersionFromCatalog() throws Exception {
        run();

        verify(mZincCatalog).getVersionForBundleID(mBundleID, mDistribution);
    }

//    @Test(expected = ZincCloneBundleJob.DistributionNotFoundException.class)
//    public void throwsIfDistributionIsNotFound() throws Exception {
//        when(mZincCatalog.getVersionForBundleID(anyString(), anyString())).thenReturn(0);
//
//        run();
//    }
}
