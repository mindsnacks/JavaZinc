package com.mindsnacks.zinc.classes.data;

import com.google.common.util.concurrent.*;
import com.mindsnacks.zinc.classes.ZincJobFactory;
import com.mindsnacks.zinc.classes.ZincLogging;
import com.mindsnacks.zinc.classes.fileutils.FileHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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

    public synchronized Future<ZincCatalog> getCatalog(final SourceURL sourceURL) {
        final File catalogFile = getCatalogFile(sourceURL.getCatalogID());

        try {
            final ZincCatalog zincCatalog = readCatalogFile(catalogFile);

            final SettableFuture<ZincCatalog> future = SettableFuture.create();
            future.set(zincCatalog);

            return future;
        } catch (final FileNotFoundException e) {
            final ListenableFuture<ZincCatalog> future = mExecutorService.submit(mJobFactory.downloadCatalog(sourceURL));

            Futures.addCallback(future, new FutureCallback<ZincCatalog>() {
                @Override
                public void onSuccess(final ZincCatalog result) {
                    persistCatalog(result, catalogFile);
                }
                @Override
                public void onFailure(final Throwable t) {
                    // the download failed
                }
            });

            return future;
        }
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
