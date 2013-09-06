package com.zinc.classes.jobs;

import com.github.kevinsawicki.http.HttpRequest;
import com.google.gson.Gson;
import com.zinc.classes.ZincBundle;
import com.zinc.classes.ZincCatalog;
import com.zinc.classes.ZincFutureFactory;
import com.zinc.exceptions.ZincRuntimeException;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * User: NachoSoto
 * Date: 9/3/13
 */
public class ZincDownloader implements ZincFutureFactory {
    private static final String CATALOG_FILENAME = "catalog.json";

    private final Gson mGson;
    private final ExecutorService mExecutorService;

    public ZincDownloader(final Gson gson, final ExecutorService executorService) {
        mGson = gson;
        mExecutorService = executorService;
    }

    @Override
    public Future<ZincCatalog> downloadCatalog(final URL sourceURL) {
        final URL url;
        try {
            url = new URL(sourceURL, CATALOG_FILENAME);
        } catch (MalformedURLException e) {
            throw new ZincRuntimeException("Invalid URL: " + sourceURL + "/" + CATALOG_FILENAME, e);
        }

        return submitJob(new ZincDownloadObjectJob<ZincCatalog>(createRequestExecutor(), url, mGson, ZincCatalog.class));
    }

    @Override
    public Future<File> downloadArchive(final URL url, final File root, final String child) {
        return submitJob(new ZincDownloadArchiveJob(createRequestExecutor(), url, root, child));
    }

    @Override
    public Future<ZincBundle> cloneBundle(final Set<URL> sourceURLs,
                                          final String bundleID,
                                          final String distribution,
                                          final Future<ZincCatalog> catalog,
                                          final File repoFolder) {
        return submitJob(new ZincCloneBundleJob(sourceURLs, bundleID, distribution, catalog, this, repoFolder));
    }

    private ZincRequestExecutor createRequestExecutor() {
        return new ZincRequestExecutor() {
            @Override
            public InputStream get(final URL url) throws AbstractZincDownloadJob.DownloadFileError {
                try {
                    return getRequest(url).stream();
                } catch (HttpRequest.HttpRequestException e) {
                    throw new AbstractZincDownloadJob.DownloadFileError("Error downloading file at url '" + url + "'", e);
                }
            }

            private HttpRequest getRequest(final URL url) {
                return HttpRequest.get(url).acceptGzipEncoding().uncompress(true);
            }
        };
    }

    private <V> Future<V> submitJob(final ZincJob<V> job) {
        return mExecutorService.submit(job);
    }
}