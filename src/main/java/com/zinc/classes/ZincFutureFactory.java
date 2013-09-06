package com.zinc.classes;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

/**
* User: NachoSoto
* Date: 9/5/13
*/
public interface ZincFutureFactory {
    Future<ZincCatalog> downloadCatalog(final URL sourceURL);
    Future<File> downloadArchive(URL url, File root, String child);
    Future<ZincBundle> cloneBundle(Set<URL> sourceURLs,
                                   String bundleID,
                                   String distribution,
                                   Future<ZincCatalog> catalog,
                                   File repoFolder);
}