package com.mindsnacks.zinc.classes.data;

import com.google.common.util.concurrent.SettableFuture;
import com.google.gson.Gson;
import com.mindsnacks.zinc.classes.ZincJobFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.concurrent.Future;

/**
 * @author Nacho Soto
 *
 * This class deals with persisting and scheduling downloading catalogs.
 */
public class ZincCatalogs {
    private final File mRoot;
    private final Gson mGson;
    private final ZincJobFactory mJobFactory;

    public ZincCatalogs(final File root, final Gson gson, final ZincJobFactory jobFactory) {
        mRoot = root;
        mGson = gson;
        mJobFactory = jobFactory;
    }

    public Future<ZincCatalog> getCatalog(final String catalogID) {
        final File catalogFile = getCatalogFile(catalogID);

        try {
            final ZincCatalog zincCatalog = readCatalogFile(catalogFile);

            final SettableFuture<ZincCatalog> future = SettableFuture.create();
            future.set(zincCatalog);

            return future;
        } catch (FileNotFoundException e) {
            // TODO: create download job
            return null;
        }
    }

    private File getCatalogFile(final String catalogID) {
        return new File(mRoot, PathHelper.getLocalCatalogFilePath(catalogID));
    }

    private ZincCatalog readCatalogFile(final File catalogFile) throws FileNotFoundException {
        return mGson.fromJson(new BufferedReader(new FileReader(catalogFile)), ZincCatalog.class);
    }
}
