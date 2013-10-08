package com.mindsnacks.zinc.classes.data;

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

    public ZincCatalogs(final File root, final FileHelper fileHelper, final ZincJobFactory jobFactory) {
        mRoot = root;
        mFileHelper = fileHelper;
        mJobFactory = jobFactory;
    }

    public Future<ZincCatalog> getCatalog(final String catalogID) {
        final File catalogFile = getCatalogFile(catalogID);

        try {
            final ZincCatalog zincCatalog = readCatalogFile(catalogFile);

            final SettableFuture<ZincCatalog> future = SettableFuture.create();
            future.set(zincCatalog);

            return future;
        } catch (final FileNotFoundException e) {
            // TODO: create download job
            return null;
        }
    }

    private File getCatalogFile(final String catalogID) {
        return new File(mRoot, PathHelper.getLocalCatalogFilePath(catalogID));
    }

    private ZincCatalog readCatalogFile(final File catalogFile) throws FileNotFoundException {
        return mFileHelper.readJSON(catalogFile, ZincCatalog.class);
    }
}
