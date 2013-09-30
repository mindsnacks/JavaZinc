package com.zinc.classes.jobs;

import com.zinc.classes.ZincJobFactory;
import com.zinc.classes.data.*;

import java.io.File;
import java.net.URL;

/**
 * @author NachoSoto
 *
 * This job downloads the appropriate bundle for the ZincCloneBundleRequest
 */
public class ZincDownloadBundleJob extends ZincJob<ZincBundle> {
    protected final ZincJobFactory mJobFactory;
    private final ZincCloneBundleRequest mRequest;

    public ZincDownloadBundleJob(final ZincCloneBundleRequest request,
                                 final ZincJobFactory jobFactory) {
        mRequest = request;
        mJobFactory = jobFactory;
    }

    @Override
    public ZincBundle run() throws Exception {
        final ZincCatalog catalog = mJobFactory.downloadCatalog(mRequest.getSourceURL()).call();

        final BundleID bundleID = mRequest.getBundleID();

        final String bundleName = bundleID.getBundleName();
        final int version = catalog.getVersionForBundleName(bundleName, mRequest.getDistribution());

        final URL archiveURL = mRequest.getSourceURL().getArchiveURL(bundleName, version, mRequest.getFlavorName());
        final String folderName = SourceURL.getLocalDownloadsFolder(bundleID, version, mRequest.getFlavorName());

        final File file = mJobFactory.downloadArchive(archiveURL, mRequest.getRepoFolder(), folderName, false).call();

        return new ZincBundle(file, bundleID, version);
    }

    @Override
    protected String getJobName() {
        return super.getJobName() + " (" + mRequest.getBundleID() + ")";
    }
}
