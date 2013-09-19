package com.zinc.classes.jobs;

import com.zinc.classes.ZincFutureFactory;
import com.zinc.classes.data.ZincBundle;
import com.zinc.classes.data.ZincCloneBundleRequest;
import com.zinc.classes.data.ZincCatalog;

import java.util.concurrent.Future;

/**
 * User: NachoSoto
 * Date: 9/13/13
 *
 * This class groups ZincDownloadBundleJob and ZincUnarchiveBundleJob.
 */
public class ZincCloneBundleJob extends ZincJob<ZincBundle> {
    private final ZincCloneBundleRequest mRequest;
    private final Future<ZincCatalog> mCatalogFuture;
    private final ZincFutureFactory mFutureFactory;

    public ZincCloneBundleJob(final ZincCloneBundleRequest request,
                              final Future<ZincCatalog> catalogFuture,
                              final ZincFutureFactory futureFactory) {
        mRequest = request;
        mCatalogFuture = catalogFuture;
        mFutureFactory = futureFactory;
    }

    @Override
    protected ZincBundle run() throws Exception {
        return mFutureFactory.unarchiveBundle(mFutureFactory.downloadBundle(mRequest, mCatalogFuture), mRequest).get();
    }
}
