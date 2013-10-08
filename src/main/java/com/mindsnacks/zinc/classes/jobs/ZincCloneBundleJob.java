package com.mindsnacks.zinc.classes.jobs;

import com.mindsnacks.zinc.classes.ZincJobFactory;
import com.mindsnacks.zinc.classes.data.ZincBundle;
import com.mindsnacks.zinc.classes.data.ZincCatalog;
import com.mindsnacks.zinc.classes.data.ZincCloneBundleRequest;

import java.util.concurrent.Future;

/**
 * User: NachoSoto
 * Date: 9/13/13
 *
 * This class groups ZincDownloadBundleJob and ZincUnarchiveBundleJob.
 */
public class ZincCloneBundleJob extends ZincJob<ZincBundle> {
    private final ZincCloneBundleRequest mRequest;
    private final ZincJobFactory mJobFactory;
    private final Future<ZincCatalog> mCatalogFuture;

    public ZincCloneBundleJob(final ZincCloneBundleRequest request,
                              final ZincJobFactory jobFactory,
                              final Future<ZincCatalog> catalogFuture) {
        mRequest = request;
        mJobFactory = jobFactory;
        mCatalogFuture = catalogFuture;
    }

    @Override
    protected ZincBundle run() throws Exception {
        return mJobFactory.unarchiveBundle(mJobFactory.downloadBundle(mRequest, mCatalogFuture).call(), mRequest).call();
    }

    @Override
    protected String getJobName() {
        return super.getJobName() + " (" + mRequest.getBundleID() + ")";
    }
}
