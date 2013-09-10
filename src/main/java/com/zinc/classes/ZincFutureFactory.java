package com.zinc.classes;

import com.zinc.classes.data.BundleID;
import com.zinc.classes.data.SourceURL;
import com.zinc.classes.data.ZincBundle;
import com.zinc.classes.data.ZincCatalog;

import java.io.File;
import java.net.URL;
import java.util.concurrent.Future;

/**
* User: NachoSoto
* Date: 9/5/13
*/
public interface ZincFutureFactory {
    Future<ZincCatalog> downloadCatalog(SourceURL sourceURL);
    Future<File> downloadArchive(URL url, File root, String child, boolean override);
    Future<ZincBundle> cloneBundle(SourceURL sourceURL,
                                   BundleID bundleID,
                                   String distribution,
                                   String flavorName,
                                   File repoFolder,
                                   Future<ZincCatalog> catalog);
}
