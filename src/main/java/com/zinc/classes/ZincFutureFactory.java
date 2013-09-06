package com.zinc.classes;

import java.io.File;
import java.net.URL;
import java.util.concurrent.Future;

/**
* User: NachoSoto
* Date: 9/5/13
*/
public interface ZincFutureFactory {
    Future<ZincCatalog> downloadCatalog(final URL sourceURL);
    Future<File> downloadArchive(final URL url, final File root, final String child);
}
