package com.zinc.classes.jobs;

import com.zinc.classes.ZincFutureFactory;
import com.zinc.classes.data.ZincBundle;
import com.zinc.classes.data.ZincBundleCloneRequest;
import com.zinc.classes.data.ZincManifest;
import com.zinc.classes.fileutils.FileHelper;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

/**
 * User: NachoSoto
 * Date: 9/10/13
 */
public class ZincUnarchiveBundleJob extends ZincJob<ZincBundle> {
    private final Future<ZincBundle> mBundle;
    private final ZincBundleCloneRequest mBundleCloneRequest;
    private final ZincFutureFactory mFutureFactory;
    private final FileHelper mFileHelper;

    public ZincUnarchiveBundleJob(final Future<ZincBundle> bundle,
                                  final ZincBundleCloneRequest bundleCloneRequest,
                                  final ZincFutureFactory futureFactory,
                                  final FileHelper fileHelper) {
        mBundle = bundle;
        mBundleCloneRequest = bundleCloneRequest;
        mFutureFactory = futureFactory;
        mFileHelper = fileHelper;
    }

    @Override
    public ZincBundle run() throws Exception {
        final ZincBundle result = mBundle.get();

        final ZincManifest manifest = mFutureFactory.downloadManifest(
                mBundleCloneRequest.getSourceURL(),
                mBundleCloneRequest.getBundleID().getBundleName(),
                result.getVersion()
        ).get();

        logMessage("unarchiving");

        final Map<String,ZincManifest.FileInfo> files = manifest.getFilesWithFlavor(mBundleCloneRequest.getFlavorName());
        final Set<String> filenamesToRemove = new HashSet<String>();

        for (final Map.Entry<String, ZincManifest.FileInfo> entry : files.entrySet()) {
            final ZincManifest.FileInfo fileInfo = entry.getValue();

            final String originFilename = fileInfo.getHashWithExtension();
            final String destinationFilename = entry.getKey();

            if (fileInfo.isGzipped()) {
                mFileHelper.unzipFile(result, originFilename, destinationFilename);
            } else {
                mFileHelper.copyFile(result, originFilename, destinationFilename);
            }

            filenamesToRemove.add(originFilename);
        }

        logMessage("cleaning up archive");

        for (final String filename : filenamesToRemove) {
            mFileHelper.removeFile(result, filename);
        }

        logMessage("finished unarchiving");

        return result;
    }

    @Override
    protected String getJobName() {
        return super.getJobName() + " (" + mBundleCloneRequest.getBundleID() + ")";
    }
}
