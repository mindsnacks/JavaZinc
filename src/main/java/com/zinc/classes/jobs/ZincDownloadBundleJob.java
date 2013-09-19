package com.zinc.classes.jobs;

import com.zinc.classes.ZincFutureFactory;
import com.zinc.classes.data.*;

import java.io.File;
import java.net.URL;
import java.util.concurrent.Future;

/**
 * User: NachoSoto
 * Date: 9/4/13
 */
public class ZincDownloadBundleJob extends ZincJob<ZincBundle> {
    protected final ZincFutureFactory mFutureFactory;
    private final ZincCloneBundleRequest mRequest;
    private final Future<ZincCatalog> mCatalogFuture;

    public ZincDownloadBundleJob(final ZincCloneBundleRequest request,
                                 final Future<ZincCatalog> catalogFuture,
                                 final ZincFutureFactory futureFactory) {
        assert request.getSourceURL().getCatalogID().equals(request.getBundleID().getCatalogID());

        mRequest = request;
        mCatalogFuture = catalogFuture;
        mFutureFactory = futureFactory;
    }

    @Override
    public ZincBundle run() throws Exception {
        final ZincCatalog catalog = mCatalogFuture.get();
        final BundleID bundleID = mRequest.getBundleID();

        final String bundleName = bundleID.getBundleName();
        final int version = catalog.getVersionForBundleName(bundleName, mRequest.getDistribution());

        final URL archiveURL = mRequest.getSourceURL().getArchiveURL(bundleName, version, mRequest.getFlavorName());
        final String folderName = SourceURL.getLocalDownloadsFolder(bundleID, version, mRequest.getFlavorName());

        final Future<File> job = mFutureFactory.downloadArchive(archiveURL, mRequest.getRepoFolder(), folderName, false);

        return new ZincBundle(job.get(), bundleID, version);
    }

    @Override
    protected String getJobName() {
        return super.getJobName() + " (" + mRequest.getBundleID() + ")";
    }
}
