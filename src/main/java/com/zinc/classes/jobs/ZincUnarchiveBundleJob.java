package com.zinc.classes.jobs;

import com.zinc.classes.ZincFutureFactory;
import com.zinc.classes.data.*;
import com.zinc.classes.fileutils.FileHelper;

import java.io.File;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * User: NachoSoto
 * Date: 9/10/13
 */
public class ZincUnarchiveBundleJob extends ZincJob<ZincBundle> {
    private final Future<ZincBundle> mDownloadedBundle;
    private final ZincBundleCloneRequest mBundleCloneRequest;
    private final ZincFutureFactory mFutureFactory;
    private final FileHelper mFileHelper;

    public ZincUnarchiveBundleJob(final Future<ZincBundle> downloadedBundle,
                                  final ZincBundleCloneRequest bundleCloneRequest,
                                  final ZincFutureFactory futureFactory,
                                  final FileHelper fileHelper) {
        mDownloadedBundle = downloadedBundle;
        mBundleCloneRequest = bundleCloneRequest;
        mFutureFactory = futureFactory;
        mFileHelper = fileHelper;
    }

    @Override
    public ZincBundle run() throws Exception {
        final ZincBundle downloadedBundle = mDownloadedBundle.get();

        final int version = downloadedBundle.getVersion();
        final BundleID bundleID = mBundleCloneRequest.getBundleID();

        final File localBundleFolder = new File(mBundleCloneRequest.getRepoFolder().getAbsolutePath() + "/" + SourceURL.getLocalBundlesFolder(bundleID.getBundleName(), version, mBundleCloneRequest.getFlavorName()));
        final ZincBundle result = new ZincBundle(localBundleFolder, bundleID, version);

        if (!localBundleFolder.exists()) {
            final ZincManifest manifest = mFutureFactory.downloadManifest(
                    mBundleCloneRequest.getSourceURL(),
                    bundleID.getBundleName(),
                    version
            ).get();

            logMessage("unarchiving");

            final Map<String, ZincManifest.FileInfo> files = manifest.getFilesWithFlavor(mBundleCloneRequest.getFlavorName());

            for (final Map.Entry<String, ZincManifest.FileInfo> entry : files.entrySet()) {
                final ZincManifest.FileInfo fileInfo = entry.getValue();

                final String originFilename = fileInfo.getHashWithExtension();
                final String destinationFilename = entry.getKey();

                if (fileInfo.isGzipped()) {
                    mFileHelper.unzipFile(downloadedBundle, originFilename, result, destinationFilename);
                } else {
                    mFileHelper.copyFile(downloadedBundle, originFilename, result, destinationFilename);
                }
            }

            logMessage("cleaning up archive");

            mFileHelper.removeFile(downloadedBundle);

            logMessage("finished unarchiving");
        } else {
            logMessage("skipping unarchiving - bundle already found");
        }

        return result;
    }

    @Override
    protected String getJobName() {
        return super.getJobName() + " (" + mBundleCloneRequest.getBundleID() + ")";
    }
}
