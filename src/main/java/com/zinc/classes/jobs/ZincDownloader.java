package com.zinc.classes.jobs;

import com.github.kevinsawicki.http.HttpRequest;
import com.google.gson.Gson;
import com.zinc.classes.ZincFutureFactory;
import com.zinc.classes.ZincLogging;
import com.zinc.classes.data.*;
import com.zinc.classes.fileutils.FileHelper;
import com.zinc.exceptions.ZincRuntimeException;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * User: NachoSoto
 * Date: 9/3/13
 */
public class ZincDownloader implements ZincFutureFactory {
    private final Gson mGson;
    private final ExecutorService mMainExecutorService;
    private final ExecutorService mLimitedConcurrencyExecutorService;

    public ZincDownloader(final Gson gson,
                          final ExecutorService mainExecutorService,
                          final ExecutorService limitedConcurrencyExecutorService) {
        mGson = gson;
        mMainExecutorService = mainExecutorService;
        mLimitedConcurrencyExecutorService = limitedConcurrencyExecutorService;
    }

    @Override
    public Future<ZincCatalog> downloadCatalog(final SourceURL sourceURL) {
        final URL url;
        try {
            url = sourceURL.getCatalogFileURL();
        } catch (MalformedURLException e) {
            throw new ZincRuntimeException("Error getting catalog file URL for source: " + sourceURL, e);
        }

        return submitJob(new ZincDownloadObjectJob<ZincCatalog>(createRequestExecutor(), url, mGson, ZincCatalog.class), false);
    }

    @Override
    public Future<ZincManifest> downloadManifest(final SourceURL sourceURL, final String bundleName, final int version) {
        final URL manifestFileURL;
        try {
            manifestFileURL = sourceURL.getManifestFileURL(bundleName, version);
        } catch (MalformedURLException e) {
            throw new ZincRuntimeException("Invalid manifest URL: " + sourceURL, e);
        }
        return submitJob(new ZincDownloadObjectJob<ZincManifest>(createRequestExecutor(), manifestFileURL, mGson, ZincManifest.class), false);
    }

    @Override
    public Future<File> downloadArchive(final URL url, final File root, final String child, final boolean override) {
        return submitJob(new ZincDownloadArchiveJob(createRequestExecutor(), url, root, child, override), true);
    }

    @Override
    public Future<ZincBundle> cloneBundle(final ZincCloneBundleRequest request,
                                          final Future<ZincCatalog> catalogFuture) {
        return submitJob(new ZincCloneBundleJob(request, catalogFuture, this), false);
    }

    @Override
    public Future<ZincBundle> downloadBundle(final ZincCloneBundleRequest request,
                                             final Future<ZincCatalog> catalogFuture) {
        return submitJob(new ZincDownloadBundleJob(request, catalogFuture, this), false);
    }

    @Override
    public Future<ZincBundle> unarchiveBundle(final Future<ZincBundle> downloadedBundle,
                                              final ZincCloneBundleRequest request) {
        return submitJob(new ZincUnarchiveBundleJob(downloadedBundle, request, this, new FileHelper()), false);
    }

    private <V> Future<V> submitJob(final ZincJob<V> job, boolean limitedConcurrency) {
        return executorService(limitedConcurrency).submit(job);
    }

    private ExecutorService executorService(boolean limitedConcurrency) {
        return (limitedConcurrency) ? mLimitedConcurrencyExecutorService : mMainExecutorService;
    }

    private ZincRequestExecutor createRequestExecutor() {
        return new ZincRequestExecutor() {
            @Override
            public InputStream get(final URL url) throws AbstractZincDownloadJob.DownloadFileError {
                ZincLogging.log("ZincRequestExcecutor", "Downloading " + url);

                try {
                    final HttpRequest request = getRequest(url);
                    final int code = request.code();

                    if (code == HttpURLConnection.HTTP_OK) {
                        return request.buffer();
                    } else {
                        throw new AbstractZincDownloadJob.DownloadFileError(String.format("Error downloading file at url '%s'. Status code: %d", url, code));
                    }
                } catch (HttpRequest.HttpRequestException e) {
                    throw new AbstractZincDownloadJob.DownloadFileError("Error downloading file at url '" + url + "'", e);
                }
            }

            private HttpRequest getRequest(final URL url) {
                return HttpRequest.get(url).acceptGzipEncoding().uncompress(true);
            }
        };
    }
}
