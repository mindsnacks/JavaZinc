package com.mindsnacks.zinc.classes;

import com.mindsnacks.zinc.classes.data.ZincBundle;
import com.mindsnacks.zinc.classes.data.ZincCatalogsCache;
import com.mindsnacks.zinc.classes.data.ZincCloneBundleRequest;
import com.mindsnacks.zinc.classes.data.ZincManifests;
import com.mindsnacks.zinc.classes.data.ZincManifestsCache;
import com.mindsnacks.zinc.classes.downloads.PriorityJobQueue;

import java.util.concurrent.Callable;

/**
 * User: NachoSoto
 * Date: 10/8/13
 */
public class ZincBundleDownloader implements PriorityJobQueue.DataProcessor<ZincCloneBundleRequest, ZincBundle> {
    private final ZincJobFactory mJobFactory;
    private final ZincCatalogsCache mCatalogs;
    private final ZincManifestsCache mManifests;

    public ZincBundleDownloader(final ZincJobFactory jobFactory,
                                final ZincCatalogsCache catalogs,
                                final ZincManifestsCache manifests) {
        mJobFactory = jobFactory;
        mCatalogs = catalogs;
        mManifests = manifests;
    }

    @Override
    public Callable<ZincBundle> process(final ZincCloneBundleRequest request) {
        return mJobFactory.cloneBundle(request, mCatalogs.getCatalog(request.getSourceURL()), mManifests);
    }
}
