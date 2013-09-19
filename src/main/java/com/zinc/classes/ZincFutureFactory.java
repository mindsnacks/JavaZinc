package com.zinc.classes;

import com.zinc.classes.data.*;

import java.io.File;
import java.net.URL;
import java.util.concurrent.Future;

/**
* User: NachoSoto
* Date: 9/5/13
*/
public interface ZincFutureFactory {
    Future<ZincCatalog> downloadCatalog(SourceURL sourceURL);
    Future<ZincManifest> downloadManifest(SourceURL sourceURL, String bundleName, int version);
    Future<File> downloadArchive(URL url, File root, String child, boolean override);
    Future<ZincBundle> cloneBundle(ZincCloneBundleRequest request,
                                   Future<ZincCatalog> catalogFuture);
    Future<ZincBundle> downloadBundle(ZincCloneBundleRequest request,
                                      Future<ZincCatalog> catalogFuture);
    Future<ZincBundle> unarchiveBundle(Future<ZincBundle> downloadedBundle,
                                       ZincCloneBundleRequest request);
}
