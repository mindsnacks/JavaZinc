package com.mindsnacks.zinc.classes;

import com.mindsnacks.zinc.classes.data.*;

import java.io.File;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
* User: NachoSoto
* Date: 9/5/13
*/
public interface ZincJobFactory {
    Callable<ZincCatalog> downloadCatalog(SourceURL sourceURL);
    Callable<ZincManifest> downloadManifest(SourceURL sourceURL, String bundleName, int version);
    Callable<File> downloadArchive(URL url, File root, String child, boolean override);
    Callable<ZincBundle> cloneBundle(ZincCloneBundleRequest request, Future<ZincCatalog> catalogFuture);
    Callable<ZincBundle> downloadBundle(ZincCloneBundleRequest request, Future<ZincCatalog> catalogFuture);
    Callable<File> downloadFile(URL url, File root, String child, boolean override, String expectedHash);
    Callable<ZincBundle> unarchiveBundle(ZincBundle downloadedBundle, ZincCloneBundleRequest request, ZincManifest manifest);
}
