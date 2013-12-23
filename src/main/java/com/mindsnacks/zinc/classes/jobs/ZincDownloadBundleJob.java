package com.mindsnacks.zinc.classes.jobs;

import com.mindsnacks.zinc.classes.ZincJobFactory;
import com.mindsnacks.zinc.classes.data.*;

import java.io.File;
import java.net.URL;
import java.util.concurrent.Future;

/**
 * @author NachoSoto
 *
 * This job downloads the appropriate bundle for the ZincCloneBundleRequest
 */
public class ZincDownloadBundleJob extends ZincJob<ZincBundle> {
    protected final ZincJobFactory mJobFactory;

    private final ZincCloneBundleRequest mRequest;
    private final Future<ZincCatalog> mCatalogFuture;

    public ZincDownloadBundleJob(final ZincCloneBundleRequest request,
                                 final ZincJobFactory jobFactory,
                                 final Future<ZincCatalog> catalogFuture) {
        mRequest = request;
        mJobFactory = jobFactory;
        mCatalogFuture = catalogFuture;
    }

    @Override
    public ZincBundle run() throws Exception {
        final ZincCatalog catalog = mCatalogFuture.get();

        final BundleID bundleID = mRequest.getBundleID();

        final String bundleName = bundleID.getBundleName();
        final int version = catalog.getVersionForBundleName(bundleName, mRequest.getDistribution());

        final URL archiveURL = mRequest.getSourceURL().getArchiveURL(bundleName, version, mRequest.getFlavorName());
        final String folderName = PathHelper.getLocalDownloadFolder(bundleID, version, mRequest.getFlavorName());

        final File file = mJobFactory.downloadArchive(archiveURL, mRequest.getRepoFolder(), folderName, true).call();

        return new ZincBundle(file, bundleID, version);
    }

    @Override
    protected String getJobName() {
        return super.getJobName() + " (" + mRequest.getBundleID() + ")";
    }
}
