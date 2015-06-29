package com.mindsnacks.zinc.classes.data;

import com.google.common.util.concurrent.*;
import com.google.gson.JsonSyntaxException;
import com.mindsnacks.zinc.classes.ZincJobFactory;
import com.mindsnacks.zinc.classes.ZincLogging;
import com.mindsnacks.zinc.classes.fileutils.FileHelper;
import com.mindsnacks.zinc.exceptions.ZincRuntimeException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author Nacho Soto
 *
 * This class deals with persisting and scheduling downloading catalogs.
 */
public class ZincCatalogs implements ZincCatalogsCache {
    private static final long INITIAL_UPDATE_DELAY = TimeUnit.MINUTES.toMillis(5);
    private static final long UPDATE_FREQUENCY = TimeUnit.MINUTES.toMillis(5);

    private final File mRoot;
    private final FileHelper mFileHelper;

    private final Set<SourceURL> mTrackedSourceURLs;

    private final ZincJobFactory mJobFactory;
    private final ListeningExecutorService mDownloadExecutorService;
    private final ExecutorService mPersistenceExecutorService;
    private final Timer mUpdateTimer;

    private final Map<SourceURL, ListenableFuture<ZincCatalog>> mFutures = new HashMap<SourceURL, ListenableFuture<ZincCatalog>>();
    private boolean mUpdateScheduled = false;

    public ZincCatalogs(final File root,
                        final FileHelper fileHelper,
                        final Set<SourceURL> trackedSourceURLs,
                        final ZincJobFactory jobFactory,
                        final ExecutorService downloadExecutorService,
                        final ExecutorService persistenceExecutorService,
                        final Timer timer) {
        mRoot = root;
        mFileHelper = fileHelper;

        mTrackedSourceURLs = trackedSourceURLs;

        mJobFactory = jobFactory;
        mDownloadExecutorService = MoreExecutors.listeningDecorator(downloadExecutorService);
        mPersistenceExecutorService = persistenceExecutorService;
        mUpdateTimer = timer;
    }

    @Override
    public synchronized Future<ZincCatalog> getCatalog(final SourceURL sourceURL) {
        mTrackedSourceURLs.add(sourceURL);

        if (!mFutures.containsKey(sourceURL)) {
            ListenableFuture<ZincCatalog> result;

            final File catalogFile = getCatalogFile(sourceURL);

            try {
                result = getPersistedCatalog(sourceURL, catalogFile);
            } catch (final FileNotFoundException | JsonSyntaxException e) {
                result = downloadCatalog(sourceURL, catalogFile);
            }

            cacheFuture(sourceURL, result);
        }

        return mFutures.get(sourceURL);
    }

    /**
     * Must be called before scheduling updates.
     */
    @Override
    public synchronized boolean clearCachedCatalogs() {
        if (mUpdateScheduled) {
            throw new ZincRuntimeException("Updates were already scheduled");
        }

        return mFileHelper.emptyDirectory(getCatalogsFolder());
    }

    @Override
    public void scheduleUpdate() {
        if (!mUpdateScheduled) {
            mUpdateScheduled = true;

            mUpdateTimer.schedule(new TimerTask() {
                @Override public void run() {
                    updateCatalogsForTrackedSourceURLs();
                }
            }, INITIAL_UPDATE_DELAY, UPDATE_FREQUENCY);
        }
    }

    private synchronized SettableFuture<ZincCatalog> getPersistedCatalog(final SourceURL sourceURL, final File catalogFile) throws FileNotFoundException {
        final ZincCatalog zincCatalog = readCatalogFile(catalogFile);

        final SettableFuture<ZincCatalog> future = SettableFuture.create();
        future.set(zincCatalog);

        logMessage(sourceURL.getCatalogID(), "Returning persisted catalog");

        return future;
    }

    private synchronized ListenableFuture<ZincCatalog> downloadCatalog(final SourceURL sourceURL, final File catalogFile) {
        final ListenableFuture<ZincCatalog> originalFuture = mFutures.get(sourceURL);
        final ListenableFuture<ZincCatalog> result = mDownloadExecutorService.submit(mJobFactory.downloadCatalog(sourceURL));

        Futures.addCallback(result, new FutureCallback<ZincCatalog>() {
            @Override public void onSuccess(final ZincCatalog result) {
                persistCatalog(result, catalogFile);
            }

            @Override public void onFailure(final Throwable downloadFailed) {
                logMessage(sourceURL.getCatalogID(), "Failed to download");

                if (originalFuture != null) {
                    cacheFuture(sourceURL, originalFuture);
                } else {
                    removeFuture(sourceURL);
                }
            }
        }, mPersistenceExecutorService);

        return result;
    }

    private synchronized void persistCatalog(final ZincCatalog result, final File catalogFile) {
        try {
            logMessage(result.getIdentifier(), "Persisting catalog to disk: " + result.getIdentifier());

            mFileHelper.writeObject(catalogFile, result, ZincCatalog.class);
        } catch (final IOException e) {
            logMessage(result.getIdentifier(), "Error persisting catalog to disk: " + e);
        }
    }

    private synchronized void cacheFuture(final SourceURL sourceURL, final ListenableFuture<ZincCatalog> future) {
        mFutures.put(sourceURL, future);
    }

    private synchronized void removeFuture(final SourceURL sourceURL) {
        mFutures.remove(sourceURL);
    }

    private synchronized void updateCatalogsForTrackedSourceURLs() {
        logMessage("All", "Updating catalogs for tracked source URLs");

        for (final SourceURL sourceURL : mTrackedSourceURLs) {
            final ListenableFuture<ZincCatalog> future = downloadCatalog(sourceURL, getCatalogFile(sourceURL));

            if (!future.isDone()) { // otherwise downloadCatalog has already cached the result
                cacheFuture(sourceURL, future);
            }
        }
    }

    private File getCatalogsFolder() {
        return new File(mRoot, PathHelper.getCatalogsFolder());
    }

    private File getCatalogFile(final SourceURL sourceURL) {
        return new File(mRoot, PathHelper.getLocalCatalogFilePath(sourceURL.getCatalogID()));
    }

    private ZincCatalog readCatalogFile(final File catalogFile) throws FileNotFoundException {
        if (catalogFile.length() == 0) {
            throw new FileNotFoundException("Catalog file is empty");
        }

        return mFileHelper.readJSON(catalogFile, ZincCatalog.class);
    }

    private void logMessage(final String catalogID, final String message) {
        ZincLogging.log(getClass().getSimpleName() + " (" + catalogID + ")", message);
    }
}
