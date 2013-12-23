package com.mindsnacks.zinc.classes.jobs;

import com.mindsnacks.zinc.classes.ZincJobFactory;
import com.mindsnacks.zinc.classes.data.*;

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

        if (!localBundleFolder.exists()) {
            final ZincBundle downloadedBundle = mJobFactory.downloadBundle(mRequest, mCatalogFuture).call();
            return mJobFactory.unarchiveBundle(downloadedBundle, mRequest).call();
        } else {
            return new ZincBundle(localBundleFolder, bundleID, version);
        }
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
