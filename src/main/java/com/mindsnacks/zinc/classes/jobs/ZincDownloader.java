package com.mindsnacks.zinc.classes.jobs;

import com.github.kevinsawicki.http.HttpRequest;
import com.google.gson.Gson;
import com.mindsnacks.zinc.classes.ZincJobFactory;
import com.mindsnacks.zinc.classes.ZincLogging;
import com.mindsnacks.zinc.classes.data.*;
import com.mindsnacks.zinc.classes.fileutils.FileHelper;
import com.mindsnacks.zinc.classes.fileutils.HashUtil;
import com.mindsnacks.zinc.exceptions.ZincRuntimeException;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * User: NachoSoto
 * Date: 9/3/13
 */
public class ZincDownloader implements ZincJobFactory {
    private final Gson mGson;
    private final File mRepoFolder;

    public ZincDownloader(final Gson gson,
                          final File repoFolder) {
        mGson = gson;
        mRepoFolder = repoFolder;
    }

    @Override
    public Callable<ZincCatalog> downloadCatalog(final SourceURL sourceURL) {
        final URL url;
        try {
            url = sourceURL.getCatalogFileURL();
        } catch (MalformedURLException e) {
            throw new ZincRuntimeException("Error getting catalog file URL for source: " + sourceURL, e);
        }

        return new ZincDownloadObjectJob<>(createRequestExecutor(), url, mGson, new FileHelper(mGson, new HashUtil()), mRepoFolder, ZincCatalog.class);
    }

    @Override
    public Callable<ZincManifest> downloadManifest(final SourceURL sourceURL, final String bundleName, final int version) {
        final URL manifestFileURL;
        try {
            manifestFileURL = sourceURL.getManifestFileURL(bundleName, version);
        } catch (MalformedURLException e) {
            throw new ZincRuntimeException("Invalid manifest URL: " + sourceURL, e);
        }
        return new ZincDownloadObjectJob<>(createRequestExecutor(), manifestFileURL, mGson, new FileHelper(mGson, new HashUtil()), mRepoFolder, ZincManifest.class);
    }

    @Override
    public Callable<File> downloadArchive(final URL url, final File root, final String child, final boolean override) {
        return new ZincDownloadArchiveJob(createRequestExecutor(), url, root, child, override);
    }

    @Override
    public Callable<ZincBundle> cloneBundle(final ZincCloneBundleRequest request, final Future<ZincCatalog> catalogFuture, final ZincManifestsCache manifests) {
        FileHelper fileHelper = new FileHelper(mGson, new HashUtil());
        ZincUntrackedBundlesCleaner bundlesCleaner = new ZincUntrackedBundlesCleaner(fileHelper);

        return new ZincCloneBundleJob(request, this, catalogFuture, manifests, bundlesCleaner);
    }

    @Override
    public Callable<ZincBundle> downloadBundle(final ZincCloneBundleRequest request, final Future<ZincCatalog> catalogFuture) {
        return new ZincDownloadBundleJob(request, this, catalogFuture);
    }

    @Override
    public Callable<File> downloadFile(final URL url,
                                       final File root,
                                       final File repoFolder,
                                       final String child,
                                       final boolean override,
                                       final String expectedHash) {
        return new ZincDownloadFileJob(createRequestExecutor(), url, root, repoFolder, child, override, expectedHash, new FileHelper(mGson, new HashUtil()));
    }

    @Override
    public Callable<ZincBundle> unarchiveBundle(final ZincBundle downloadedBundle,
                                                final ZincCloneBundleRequest request,
                                                final ZincManifest manifest) {
        return new ZincUnarchiveBundleJob(downloadedBundle, request, manifest, new FileHelper(mGson, new HashUtil()));
    }

    private ZincRequestExecutor createRequestExecutor() {
        return new ZincRequestExecutor() {
            @Override
            public InputStream get(final URL url) throws AbstractZincDownloadJob.DownloadFileError {
                ZincLogging.log("ZincRequestExecutor", "Downloading " + url);

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
