package com.mindsnacks.zinc.classes.data;

import com.google.common.util.concurrent.*;
import com.mindsnacks.zinc.classes.ZincJobFactory;
import com.mindsnacks.zinc.classes.ZincLogging;
import com.mindsnacks.zinc.classes.fileutils.FileHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author Nacho Soto
 *
 * This class deals with persisting and scheduling downloading catalogs.
 *
 * TODO: cache futures, and only invalidate them when the scheduler updates all catalogs.
 * TODO: schedule updates.
 */
public class ZincCatalogs {
    private final File mRoot;
    private final FileHelper mFileHelper;
    private final ZincJobFactory mJobFactory;
    private final ListeningScheduledExecutorService mDownloadExecutorService;
    private final ExecutorService mPersistenceExecutorService;

    private final Map<SourceURL, Future<ZincCatalog>> mFutures = new HashMap<SourceURL, Future<ZincCatalog>>();

    public ZincCatalogs(final File root,
                        final FileHelper fileHelper,
                        final ZincJobFactory jobFactory,
                        final ScheduledExecutorService downloadExecutorService,
                        final ExecutorService persistenceExecutorService) {
        mRoot = root;
        mFileHelper = fileHelper;
        mJobFactory = jobFactory;
        mDownloadExecutorService = MoreExecutors.listeningDecorator(downloadExecutorService);
        mPersistenceExecutorService = persistenceExecutorService;
    }

    public synchronized Future<ZincCatalog> getCatalog(final SourceURL sourceURL) {
        if (!mFutures.containsKey(sourceURL)) {
            ListenableFuture<ZincCatalog> result;

            final File catalogFile = getCatalogFile(sourceURL.getCatalogID());

            try {
                final ZincCatalog zincCatalog = readCatalogFile(catalogFile);

                final SettableFuture<ZincCatalog> future = SettableFuture.create();
                future.set(zincCatalog);

                result = future;
            } catch (final FileNotFoundException e) {
                result = mDownloadExecutorService.submit(mJobFactory.downloadCatalog(sourceURL));

                Futures.addCallback(result, new FutureCallback<ZincCatalog>() {
                    @Override
                    public void onSuccess(final ZincCatalog result) {
                        persistCatalog(result, catalogFile);
                    }
                    @Override
                    public void onFailure(final Throwable t) {
                        // the download failed
                    }
                }, mPersistenceExecutorService);
            }

            cacheFuture(sourceURL, result);
        }

        return mFutures.get(sourceURL);
    }

    private void cacheFuture(final SourceURL sourceURL, final ListenableFuture<ZincCatalog> future) {
        mFutures.put(sourceURL, future);
    }

    private synchronized void persistCatalog(final ZincCatalog result, final File catalogFile) {
        try {
            logMessage("Persisting catalog to disk: " + result.getIdentifier());
            mFileHelper.writeObject(catalogFile, result, ZincCatalog.class);
        } catch (final IOException e) {
            logMessage("Error persisting catalog to disk: " + e);
        }
    }

    private File getCatalogFile(final String catalogID) {
        return new File(mRoot, PathHelper.getLocalCatalogFilePath(catalogID));
    }

    private ZincCatalog readCatalogFile(final File catalogFile) throws FileNotFoundException {
        return mFileHelper.readJSON(catalogFile, ZincCatalog.class);
    }

    private void logMessage(final String message) {
        ZincLogging.log(getClass().getName(), message);
    }
}
