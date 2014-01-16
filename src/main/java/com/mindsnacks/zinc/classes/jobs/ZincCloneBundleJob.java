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

        if (!localBundleFolder.exists()) { // TODO: extract this logic as a first step to implement bundle verification
            final ZincManifest manifest = getManifest();

            if (manifest.containsFiles(mRequest.getFlavorName())) {
                final ZincBundle downloadedBundle = mJobFactory.downloadBundle(mRequest, mCatalogFuture).call();
                return mJobFactory.unarchiveBundle(downloadedBundle, mRequest, manifest).call();
            } else {
                logMessage("empty bundle");

                return createZincBundle(localBundleFolder);
            }
        } else {
            logMessage("bundle already available");

            return createZincBundle(localBundleFolder);
        }
    }

    private ZincBundle createZincBundle(final File folder) {
        final ZincBundle result = new ZincBundle(folder, mBundleID, mVersion);

        if (!result.exists() && !result.mkdirs()) {
            throw new ZincRuntimeException(String.format("Error creating folder for '%s'", result));
        }

        return result;
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
