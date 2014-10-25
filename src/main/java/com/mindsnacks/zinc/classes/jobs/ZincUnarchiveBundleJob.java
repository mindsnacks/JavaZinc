package com.mindsnacks.zinc.classes.jobs;

import com.mindsnacks.zinc.classes.data.*;
import com.mindsnacks.zinc.classes.fileutils.FileHelper;
import com.mindsnacks.zinc.classes.fileutils.ValidatingDigestOutputStream;
import com.mindsnacks.zinc.exceptions.ZincRuntimeException;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * @author NachoSoto.
 *
 * This job creates the result bundle by extracting the contents of the archive,
 * with the information in the ZincManifest.
 */
public class ZincUnarchiveBundleJob extends ZincJob<ZincBundle> {
    private final ZincBundle mDownloadedBundle;
    private final ZincCloneBundleRequest mRequest;
    private final ZincManifest mManifest;
    private final FileHelper mFileHelper;

    public ZincUnarchiveBundleJob(final ZincBundle downloadedBundle,
                                  final ZincCloneBundleRequest request,
                                  final ZincManifest zincManifest,
                                  final FileHelper fileHelper) {
        mDownloadedBundle = downloadedBundle;
        mRequest = request;
        mManifest = zincManifest;
        mFileHelper = fileHelper;
    }

    @Override
    public ZincBundle run() throws Exception {
        final BundleID bundleID = mRequest.getBundleID();
        final int version = mDownloadedBundle.getVersion();

        final File temporaryFolder = getTemporaryBundleFolder(bundleID),
                   resultFolder = getBundleFolder(bundleID);

        unarchiveBundle(mDownloadedBundle, temporaryFolder, mManifest);

        cleanUpDownloadedFolder();
        moveToBundlesFolder(temporaryFolder, resultFolder);

        return new ZincBundle(resultFolder, bundleID, version);
    }

    private File getTemporaryBundleFolder(final BundleID bundleID) {
        return new File(
            mRequest.getRepoFolder(),
            PathHelper.getLocalTemporaryBundleFolder(bundleID, mDownloadedBundle.getVersion(), mRequest.getFlavorName())
        );
    }

    private File getBundleFolder(final BundleID bundleID) {
        return new File(
            mRequest.getRepoFolder(),
            PathHelper.getLocalBundleFolder(bundleID, mDownloadedBundle.getVersion(), mRequest.getFlavorName())
        );
    }

    private void unarchiveBundle(final File downloadedBundle,
                                 final File temporaryFolder,
                                 final ZincManifest manifest) throws IOException, ValidatingDigestOutputStream.HashFailedException {
        logMessage("unarchiving");

        final Map<String, ZincManifest.FileInfo> files = manifest.getFilesWithFlavor(mRequest.getFlavorName());

        for (final Map.Entry<String, ZincManifest.FileInfo> entry : files.entrySet()) {
            final ZincManifest.FileInfo fileInfo = entry.getValue();

            final String originFilename = fileInfo.getHashWithExtension();
            final String destinationFilename = entry.getKey();
            final String expectedHash = fileInfo.getHash();

            if (fileInfo.isGzipped()) {
                mFileHelper.unzipFile(downloadedBundle, originFilename, temporaryFolder, destinationFilename, expectedHash);
            } else {
                mFileHelper.copyFile(downloadedBundle, originFilename, temporaryFolder, destinationFilename, expectedHash);
            }
        }
    }

    private void cleanUpDownloadedFolder() {
        logMessage("cleaning up archive");

        mFileHelper.removeDirectory(mDownloadedBundle);
    }

    private void moveToBundlesFolder(final File temporaryFolder, final File bundleFolder) {
        logMessage("moving bundle");

        if ((!bundleFolder.exists() && !bundleFolder.mkdirs()) || !mFileHelper.moveFile(temporaryFolder, bundleFolder)) {
            throw new ZincRuntimeException(String.format("Error moving bundle from '%s' to '%s'", temporaryFolder, bundleFolder));
        }
    }

    @Override
    protected String getJobName() {
        return super.getJobName() + " (" + mRequest.getBundleID() + ")";
    }
}
