package com.zinc.classes.jobs;

import com.zinc.classes.ZincFutureFactory;
import com.zinc.classes.data.ZincBundle;
import com.zinc.classes.data.ZincBundleCloneRequest;

import java.util.concurrent.Future;

/**
 * User: NachoSoto
 * Date: 9/10/13
 */
public class ZincUnarchiveBundleJob implements ZincJob<ZincBundle> {
    private final Future<ZincBundle> mBundle;
    private final ZincBundleCloneRequest mBundleCloneRequest;
    private final ZincFutureFactory mFutureFactory;

    public ZincUnarchiveBundleJob(final Future<ZincBundle> bundle,
                                  final ZincBundleCloneRequest bundleCloneRequest,
                                  final ZincFutureFactory futureFactory) {
        mBundle = bundle;
        mBundleCloneRequest = bundleCloneRequest;
        mFutureFactory = futureFactory;
    }

    @Override
    public ZincBundle call() throws Exception {
        final ZincBundle result = mBundle.get();

        return result;
    }
}
