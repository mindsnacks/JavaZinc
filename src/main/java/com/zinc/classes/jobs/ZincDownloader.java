package com.zinc.classes.jobs;

import com.github.kevinsawicki.http.HttpRequest;
import com.google.gson.Gson;
import com.zinc.classes.ZincFutureFactory;
import com.zinc.classes.data.BundleID;
import com.zinc.classes.data.SourceURL;
import com.zinc.classes.data.ZincBundle;
import com.zinc.classes.data.ZincCatalog;
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
    private final ExecutorService mExecutorService;

    public ZincDownloader(final Gson gson, final ExecutorService executorService) {
        mGson = gson;
        mExecutorService = executorService;
    }

    @Override
    public Future<ZincCatalog> downloadCatalog(final SourceURL sourceURL) {
        final URL url;
        try {
            url = sourceURL.getCatalogFileURL();
        } catch (MalformedURLException e) {
            throw new ZincRuntimeException("Error getting catalog file URL for source: " + sourceURL, e);
        }

        return submitJob(new ZincDownloadObjectJob<ZincCatalog>(createRequestExecutor(), url, mGson, ZincCatalog.class));
    }

    @Override
    public Future<File> downloadArchive(final URL url, final File root, final String child, final boolean override) {
        return submitJob(new ZincDownloadArchiveJob(createRequestExecutor(), url, root, child, override));
    }

    @Override
    public Future<ZincBundle> cloneBundle(final SourceURL sourceURL,
                                          final BundleID bundleID,
                                          final String distribution,
                                          final String flavorName,
                                          final File repoFolder,
                                          final Future<ZincCatalog> catalog) {
        return submitJob(new ZincCloneBundleJob(sourceURL, bundleID, distribution, flavorName, catalog, this, repoFolder));
    }

    private ZincRequestExecutor createRequestExecutor() {
        return new ZincRequestExecutor() {
            @Override
            public InputStream get(final URL url) throws AbstractZincDownloadJob.DownloadFileError {
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

    private <V> Future<V> submitJob(final ZincJob<V> job) {
        return mExecutorService.submit(job);
    }
}
