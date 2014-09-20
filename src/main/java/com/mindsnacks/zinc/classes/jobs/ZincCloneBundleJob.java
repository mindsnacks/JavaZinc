package com.mindsnacks.zinc.classes.jobs;

import com.mindsnacks.zinc.classes.ZincJobFactory;
import com.mindsnacks.zinc.classes.data.*;
import com.mindsnacks.zinc.classes.fileutils.HashUtil;
import com.mindsnacks.zinc.exceptions.ZincRuntimeException;

import java.io.File;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * @author NachoSoto.
 *
 * This class groups ZincDownloadBundleJob and ZincUnarchiveBundleJob.
 */
public class ZincCloneBundleJob extends ZincJob<ZincBundle> {
    private final ZincCloneBundleRequest mRequest;
    private final ZincJobFactory mJobFactory;
    private final Future<ZincCatalog> mCatalogFuture;
    private BundleID mBundleID;
    private int mVersion;

    public ZincCloneBundleJob(final ZincCloneBundleRequest request,
                              final ZincJobFactory jobFactory,
                              final Future<ZincCatalog> catalogFuture) {
        mRequest = request;
        mJobFactory = jobFactory;
        mCatalogFuture = catalogFuture;
    }

    @Override
    protected ZincBundle run() throws Exception {
        mBundleID = mRequest.getBundleID();
        mVersion = getBundleVersion(mBundleID);

        final File localBundleFolder = getLocalBundleFolder();
        final ZincManifest manifest = getManifest();
        final String flavorName = mRequest.getFlavorName();

        if (shouldDownloadBundle(localBundleFolder, manifest, flavorName)) {
            createFolder(localBundleFolder);

            if (manifest.containsFiles(flavorName)) {
                if (manifest.archiveExists(flavorName)) {
                    return downloadAndUnarchiveBundle(manifest);
                } else {
                    return createZincBundle(downloadOnlyFileInBundle(localBundleFolder, manifest, flavorName));
                }
            } else {
                logMessage("empty bundle");

                return createZincBundle(localBundleFolder);
            }
        } else {
            logMessage("bundle already available");

            return createZincBundle(localBundleFolder);
        }
    }

    private boolean shouldDownloadBundle(final File localBundleFolder, ZincManifest manifest, String flavorName) {
        if (!localBundleFolder.exists() || localBundleFolder.listFiles().length == 0) {
            return true;
        }

        //validate bundle hashes
        Map<String, ZincManifest.FileInfo> fileInfoMap = manifest.getFilesWithFlavor(flavorName);

        for (Map.Entry<String, ZincManifest.FileInfo> fileInfoEntry : fileInfoMap.entrySet()) {
            File bundlefile = new File(localBundleFolder, fileInfoEntry.getKey());
            if (!bundlefile.exists()) {
                return true;
            }
            String hash = HashUtil.sha1HashString(bundlefile);
            if (!hash.equals(fileInfoEntry.getValue().getHash())) {
                return true;
            }
        }
        return false;
    }

    private void createFolder(final File folder) {
        if (!folder.exists() && !folder.mkdirs()) {
            throw new ZincRuntimeException(String.format("Error creating folder '%s'", folder));
        }
    }

    private ZincBundle downloadAndUnarchiveBundle(final ZincManifest manifest) throws Exception {
        return mJobFactory.unarchiveBundle(
                mJobFactory.downloadBundle(mRequest, mCatalogFuture).call(),
                mRequest,
                manifest).call();
    }

    private File downloadOnlyFileInBundle(final File localBundleFolder,
                                          final ZincManifest manifest,
                                          final String flavorName) throws Exception {
        final ZincManifest.FileInfo fileInfo = manifest.getFileWithFlavor(flavorName);
        final String filename = manifest.getFilenameWithFlavor(flavorName);

        return mJobFactory.downloadFile(
                mRequest.getSourceURL().getObjectURL(fileInfo),
                localBundleFolder,
                filename,
                false).call().getParentFile();
    }

    private ZincBundle createZincBundle(final File folder) {
        return new ZincBundle(folder, mBundleID, mVersion);
    }

    private ZincManifest getManifest() throws Exception {
        return mJobFactory.downloadManifest(
                mRequest.getSourceURL(),
                mBundleID.getBundleName(),
                mVersion).call();
    }

    private File getLocalBundleFolder() {
        return new File(
            mRequest.getRepoFolder(),
            PathHelper.getLocalBundleFolder(mBundleID, mVersion, mRequest.getFlavorName())
        );
    }

    private int getBundleVersion(final BundleID bundleID) throws Exception {
        return mCatalogFuture.get().getVersionForBundleName(
                bundleID.getBundleName(),
                mRequest.getDistribution());
    }

    @Override
    protected String getJobName() {
        return super.getJobName() + " (" + mRequest.getBundleID() + ")";
    }
}
