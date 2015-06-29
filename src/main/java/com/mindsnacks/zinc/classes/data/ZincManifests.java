package com.mindsnacks.zinc.classes.data;

import com.google.common.util.concurrent.*;
import com.mindsnacks.zinc.classes.ZincJobFactory;
import com.mindsnacks.zinc.classes.ZincLogging;
import com.mindsnacks.zinc.classes.fileutils.FileHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Created by Miguel Carranza on 6/26/15.
 */
public class ZincManifests implements ZincManifestsCache {
    private final File mRoot;
    private final FileHelper mFileHelper;

    private final ZincJobFactory mJobFactory;
    private final ListeningExecutorService mDownloadExecutorService;
    private final ExecutorService mPersistenceExecutorService;

    private final Map<String, ListenableFuture<ZincManifest>> mFutures = new HashMap<String, ListenableFuture<ZincManifest>>();

    public ZincManifests(final File root,
                         final FileHelper fileHelper,
                         final ZincJobFactory jobFactory,
                         final ExecutorService downloadExecutorService,
                         final ExecutorService persistenceExecutorService) {
        mRoot = root;
        mFileHelper = fileHelper;
        mJobFactory = jobFactory;
        mDownloadExecutorService = MoreExecutors.listeningDecorator(downloadExecutorService);
        mPersistenceExecutorService = persistenceExecutorService;
    }

    @Override
    public boolean clearCachedManifests() {
        return mFileHelper.emptyDirectory(getManifestsFolder());
    }

    @Override
    public synchronized Future<ZincManifest> getManifest(final SourceURL sourceURL,
                                                         final String bundleName,
                                                         final int version) {
        String manifestID = getManifestID(bundleName, version);

        if (!mFutures.containsKey(manifestID)) {
            ListenableFuture<ZincManifest> result;

            final File manifestFile = getManifestFile(manifestID);

            try {
                result = getPersistedManifest(manifestID, manifestFile);
            } catch (final FileNotFoundException e) {
                result = downloadManifest(sourceURL,
                        bundleName,
                        version,
                        manifestFile);
            }

            cacheFuture(manifestID, result);
        }
        return mFutures.get(manifestID);
    }

    private synchronized SettableFuture<ZincManifest> getPersistedManifest(final String manifestID,
                                                                           final File manifestFile) throws FileNotFoundException {
        final ZincManifest zincManifest = readManifestFile(manifestFile);

        final SettableFuture<ZincManifest> future = SettableFuture.create();
        future.set(zincManifest);

        logMessage(manifestID, "Returning persisted manifest");

        return future;
    }

    private synchronized ListenableFuture<ZincManifest> downloadManifest(final SourceURL sourceURL,
                                                                         final String bundleName,
                                                                         final int version,
                                                                         final File manifestFile) {
        final String manifestID = getManifestID(bundleName, version);
        final ListenableFuture<ZincManifest> originalFuture = mFutures.get(manifestID);
        final ListenableFuture<ZincManifest> result = mDownloadExecutorService.submit(mJobFactory.downloadManifest(sourceURL, bundleName, version));

        Futures.addCallback(result, new FutureCallback<ZincManifest>() {
            @Override public void onSuccess(final ZincManifest result) {
                persitManifest(result, manifestFile);
            }

            @Override public void onFailure(final Throwable downloadFailed) {
                logMessage(manifestID, "Failed to download");

                if (originalFuture != null) {
                    cacheFuture(manifestID, originalFuture);
                } else {
                    removeFuture(manifestID);
                }
            }
        }, mPersistenceExecutorService);

        return result;
    }

    private synchronized void persitManifest(final ZincManifest result, final File manifestFile) {
        try {
            logMessage(result.getIdentifier(), "Persisting manifest to disk: " + result.getIdentifier());

            mFileHelper.writeObject(manifestFile, result, ZincManifest.class);
        } catch (final IOException e) {
            logMessage(result.getIdentifier(), "Error persisting manifest to disk: " + e);
        }
    }

    private synchronized void cacheFuture(final String manifestID, final ListenableFuture<ZincManifest> future) {
        mFutures.put(manifestID, future);
    }

    private synchronized void removeFuture(final String manifestID) {
        mFutures.remove(manifestID);
    }

    private File getManifestsFolder() {
        return new File(mRoot, PathHelper.getManifestsFolder());
    }

    private File getManifestFile(final String manifestID) {
        return new File(mRoot, PathHelper.getLocalManifestFilePath(manifestID));
    }

    private String getManifestID(final String bundleName,
                                 final int version) {
        return String.format("%s_%d", bundleName, version);
    }

    private ZincManifest readManifestFile(final File manifestFile) throws FileNotFoundException {
        return mFileHelper.readJSON(manifestFile, ZincManifest.class);
    }

    private void logMessage(final String manifestID, final String message) {
        ZincLogging.log(getClass().getSimpleName() + " (" + manifestID + ")", message);
    }
}
