package com.mindsnacks.zinc.classes;

import com.mindsnacks.zinc.classes.data.ZincBundle;
import com.mindsnacks.zinc.classes.data.ZincCatalogs;
import com.mindsnacks.zinc.classes.data.ZincCloneBundleRequest;
import com.mindsnacks.zinc.classes.downloads.PriorityJobQueue;

import java.util.concurrent.Callable;

/**
 * User: NachoSoto
 * Date: 10/8/13
 */
public class ZincBundleDownloader implements PriorityJobQueue.DataProcessor<ZincCloneBundleRequest, ZincBundle> {
    private final ZincJobFactory mJobFactory;
    private final ZincCatalogs mCatalogs;

    public ZincBundleDownloader(final ZincJobFactory jobFactory, final ZincCatalogs catalogs) {
        mJobFactory = jobFactory;
        mCatalogs = catalogs;
    }

    @Override
    public Callable<ZincBundle> process(final ZincCloneBundleRequest request) {
        return mJobFactory.cloneBundle(request, mCatalogs.getCatalog(request.getSourceURL()));
    }
}
