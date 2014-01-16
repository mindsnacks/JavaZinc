package com.mindsnacks.zinc.classes.jobs;

import com.mindsnacks.zinc.classes.ZincJobFactory;
import com.mindsnacks.zinc.classes.data.*;
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

    public ZincCloneBundleJob(final ZincCloneBundleRequest request,
                              final ZincJobFactory jobFactory,
                              final Future<ZincCatalog> catalogFuture) {
        mRequest = request;
        mJobFactory = jobFactory;
        mCatalogFuture = catalogFuture;
    }

    @Override
    protected ZincBundle run() throws Exception {
        final BundleID bundleID = mRequest.getBundleID();

        final int version = getBundleVersion(bundleID);

        final File localBundleFolder = getLocalBundleFolder(bundleID, version);

        if (!localBundleFolder.exists()) { // TODO: extract this logic as a first step to implement bundle verification
            final ZincManifest manifest = getManifest(version, bundleID);

            if (manifest.containsFiles(mRequest.getFlavorName())) {
                final ZincBundle downloadedBundle = mJobFactory.downloadBundle(mRequest, mCatalogFuture).call();
                return mJobFactory.unarchiveBundle(downloadedBundle, mRequest, manifest).call();
            } else {
                return createEmptyBundle(bundleID, version);
            }
        } else {
            logMessage("bundle already available");

            return new ZincBundle(localBundleFolder, bundleID, version);
        }
    }

    private ZincBundle createEmptyBundle(final BundleID bundleID, final int version) {
        final File folder = new File(mRequest.getRepoFolder(), PathHelper.getLocalBundleFolder(bundleID, version, mRequest.getFlavorName()));
        final ZincBundle result = new ZincBundle(folder, bundleID, version);

        if (!result.exists() && !result.mkdirs()) {
            throw new ZincRuntimeException(String.format("Error creating folder for '%s'", result));
        }

        return result;
    }

    private ZincManifest getManifest(final int version, final BundleID bundleID) throws Exception {
        return mJobFactory.downloadManifest(
                mRequest.getSourceURL(),
                bundleID.getBundleName(),
                version).call();
    }

    private File getLocalBundleFolder(final BundleID bundleID, final int version) {
        return new File(
            mRequest.getRepoFolder(),
            PathHelper.getLocalBundleFolder(bundleID, version, mRequest.getFlavorName())
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
