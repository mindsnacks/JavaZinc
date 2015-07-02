package com.mindsnacks.zinc.classes.jobs;

import com.mindsnacks.zinc.classes.ZincJobFactory;
import com.mindsnacks.zinc.classes.data.BundleID;
import com.mindsnacks.zinc.classes.data.PathHelper;
import com.mindsnacks.zinc.classes.data.ZincBundle;
import com.mindsnacks.zinc.classes.data.ZincCatalog;
import com.mindsnacks.zinc.classes.data.ZincCloneBundleRequest;
import com.mindsnacks.zinc.classes.data.ZincManifest;
import com.mindsnacks.zinc.classes.data.ZincManifestsCache;
import com.mindsnacks.zinc.classes.data.ZincUntrackedBundlesCleaner;
import com.mindsnacks.zinc.classes.fileutils.BundleIntegrityVerifier;
import com.mindsnacks.zinc.exceptions.ZincRuntimeException;

import java.io.File;
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
    private final ZincManifestsCache mManifests;
    private BundleID mBundleID;
    private int mVersion;
    private final ZincUntrackedBundlesCleaner mBundlesCleaner;

    public ZincCloneBundleJob(final ZincCloneBundleRequest request,
                              final ZincJobFactory jobFactory,
                              final Future<ZincCatalog> catalogFuture,
                              final ZincManifestsCache manifests,
                              final ZincUntrackedBundlesCleaner bundlesCleaner) {
        mRequest = request;
        mJobFactory = jobFactory;
        mCatalogFuture = catalogFuture;
        mManifests = manifests;
        mBundlesCleaner = bundlesCleaner;
    }

    @Override
    protected ZincBundle run() throws Exception {
        mBundleID = mRequest.getBundleID();
        mVersion = getBundleVersion(mBundleID);

        final File localBundleFolder = getLocalBundleFolder();
        final ZincManifest manifest = getManifest();
        final String flavorName = mRequest.getFlavorName();

        if (shouldDownloadBundle(localBundleFolder, manifest, flavorName)) {
            mBundlesCleaner.cleanUntrackedBundles(mRequest.getRepoFolder(), mBundleID, mVersion);
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

    private boolean shouldDownloadBundle(final File localBundleFolder,
                                         final ZincManifest manifest,
                                         final String flavorName) {
        return (!localBundleFolder.exists() ||
                localBundleFolder.listFiles().length == 0 ||
                !BundleIntegrityVerifier.isLocalBundleValid(localBundleFolder, manifest, flavorName));
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
                mRequest.getRepoFolder(),
                filename,
                false,
                fileInfo.getHash()).call().getParentFile();
    }

    private ZincBundle createZincBundle(final File folder) {
        return new ZincBundle(folder, mBundleID, mVersion);
    }

    private ZincManifest getManifest() throws Exception {
        return mManifests.getManifest(mRequest.getSourceURL(),
                                      mBundleID.getBundleName(),
                                      mVersion).get();
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
