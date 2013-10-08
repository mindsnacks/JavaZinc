package com.mindsnacks.zinc.classes.data;

import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.google.common.util.concurrent.SettableFuture;
import com.mindsnacks.zinc.classes.ZincJobFactory;
import com.mindsnacks.zinc.classes.fileutils.FileHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.concurrent.Future;

/**
 * @author Nacho Soto
 *
 * This class deals with persisting and scheduling downloading catalogs.
 */
public class ZincCatalogs {
    private final File mRoot;
    private final FileHelper mFileHelper;
    private final ZincJobFactory mJobFactory;
    private final ListeningScheduledExecutorService mExecutorService;

    public ZincCatalogs(final File root,
                        final FileHelper fileHelper,
                        final ZincJobFactory jobFactory,
                        final ListeningScheduledExecutorService executorService) {
        mRoot = root;
        mFileHelper = fileHelper;
        mJobFactory = jobFactory;
        mExecutorService = executorService;
    }

    public Future<ZincCatalog> getCatalog(final SourceURL sourceURL) {
        try {
            final ZincCatalog zincCatalog = readCatalogFile(getCatalogFile(sourceURL.getCatalogID()));

            final SettableFuture<ZincCatalog> future = SettableFuture.create();
            future.set(zincCatalog);

            return future;
        } catch (final FileNotFoundException e) {
            return mExecutorService.submit(mJobFactory.downloadCatalog(sourceURL));
        }
    }

    private File getCatalogFile(final String catalogID) {
        return new File(mRoot, PathHelper.getLocalCatalogFilePath(catalogID));
    }

    private ZincCatalog readCatalogFile(final File catalogFile) throws FileNotFoundException {
        return mFileHelper.readJSON(catalogFile, ZincCatalog.class);
    }
}
