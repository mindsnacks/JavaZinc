package com.zinc.classes.jobs;

import com.zinc.classes.ZincFutureFactory;
import com.zinc.classes.data.ZincBundle;
import com.zinc.classes.data.ZincBundleCloneRequest;
import com.zinc.classes.data.ZincManifest;
import com.zinc.classes.fileutils.GzipHelper;

import java.util.Map;
import java.util.concurrent.Future;

/**
 * User: NachoSoto
 * Date: 9/10/13
 */
public class ZincUnarchiveBundleJob implements ZincJob<ZincBundle> {
    private final Future<ZincBundle> mBundle;
    private final ZincBundleCloneRequest mBundleCloneRequest;
    private final ZincFutureFactory mFutureFactory;
    private final GzipHelper mGzipHelper;

    public ZincUnarchiveBundleJob(final Future<ZincBundle> bundle,
                                  final ZincBundleCloneRequest bundleCloneRequest,
                                  final ZincFutureFactory futureFactory,
                                  final GzipHelper gzipHelper) {
        mBundle = bundle;
        mBundleCloneRequest = bundleCloneRequest;
        mFutureFactory = futureFactory;
        mGzipHelper = gzipHelper;
    }

    @Override
    public ZincBundle call() throws Exception {
        final ZincBundle result = mBundle.get();

        final ZincManifest manifest = mFutureFactory.downloadManifest(
                mBundleCloneRequest.getSourceURL(),
                mBundleCloneRequest.getBundleID().getBundleName(),
                result.getVersion()
        ).get();

        for (final Map.Entry<String, ZincManifest.FileInfo> entry : manifest.getFilesWithFlavor(mBundleCloneRequest.getFlavorName()).entrySet()) {
            mGzipHelper.unzipFile(result, entry.getValue().getHashWithExtension(), entry.getKey());
        }

        return result;
    }
}
