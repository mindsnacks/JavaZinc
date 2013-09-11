package com.zinc.classes.jobs;

import com.zinc.classes.ZincFutureFactory;
import com.zinc.classes.data.ZincBundle;
import com.zinc.classes.data.ZincBundleCloneRequest;
import com.zinc.classes.data.ZincManifest;
import com.zinc.classes.fileutils.GzipHelper;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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

        final Set<String> filenamesToRemove = new HashSet<String>();

        for (final Map.Entry<String, ZincManifest.FileInfo> entry : manifest.getFilesWithFlavor(mBundleCloneRequest.getFlavorName()).entrySet()) {
            final ZincManifest.FileInfo fileInfo = entry.getValue();

            final String originFilename = fileInfo.getHashWithExtension();
            final String destinationFilename = entry.getKey();

            if (fileInfo.isGzipped()) {
                mGzipHelper.unzipFile(result, originFilename, destinationFilename);
                filenamesToRemove.add(originFilename);
            } else {
                mGzipHelper.moveFile(result, originFilename, destinationFilename);
            }
        }

        for (final String filename : filenamesToRemove) {
            mGzipHelper.removeFile(result, filename);
        }

        return result;
    }
}
