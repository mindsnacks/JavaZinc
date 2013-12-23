package com.mindsnacks.zinc.classes.jobs;

import com.mindsnacks.zinc.classes.ZincJobFactory;
import com.mindsnacks.zinc.classes.data.*;
import com.mindsnacks.zinc.classes.fileutils.FileHelper;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * @author NachoSoto
 *
 * This job creates the result bundle by extracting the contents of the archive,
 * with the information in the ZincManifest.
 */
public class ZincUnarchiveBundleJob extends ZincJob<ZincBundle> {
    private final ZincBundle mDownloadedBundle;
    private final ZincCloneBundleRequest mRequest;
    private final ZincJobFactory mJobFactory;
    private final FileHelper mFileHelper;

    public ZincUnarchiveBundleJob(final ZincBundle downloadedBundle,
                                  final ZincCloneBundleRequest request,
                                  final ZincJobFactory jobFactory,
                                  final FileHelper fileHelper) {
        mDownloadedBundle = downloadedBundle;
        mRequest = request;
        mJobFactory = jobFactory;
        mFileHelper = fileHelper;
    }

    @Override
    public ZincBundle run() throws Exception {
        final int version = mDownloadedBundle.getVersion();
        final BundleID bundleID = mRequest.getBundleID();

        final File localBundleFolder = new File(mRequest.getRepoFolder().getAbsolutePath() + "/" + PathHelper.getLocalBundleFolder(bundleID, version, mRequest.getFlavorName()));
        final ZincBundle result = new ZincBundle(localBundleFolder, bundleID, version);

        if (!localBundleFolder.exists()) {
            final ZincManifest manifest = getManifest(version, bundleID);

            logMessage("unarchiving");
            unarchiveBundle(mDownloadedBundle, result, manifest);

            logMessage("cleaning up archive");
            mFileHelper.removeDirectory(mDownloadedBundle);
        } else {
            logMessage("skipping unarchiving - bundle already found");
        }

        return result;
    }

    private ZincManifest getManifest(final int version,
                                     final BundleID bundleID) throws Exception {
        return mJobFactory.downloadManifest(
                mRequest.getSourceURL(),
                bundleID.getBundleName(),
                version
        ).call();
    }

    private void unarchiveBundle(final ZincBundle downloadedBundle,
                                 final ZincBundle result,
                                 final ZincManifest manifest) throws IOException {
        final Map<String, ZincManifest.FileInfo> files = manifest.getFilesWithFlavor(mRequest.getFlavorName());

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
    }

    @Override
    protected String getJobName() {
        return super.getJobName() + " (" + mRequest.getBundleID() + ")";
    }
}
