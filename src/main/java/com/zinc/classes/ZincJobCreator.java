package com.zinc.classes;

import com.zinc.classes.jobs.ZincJob;

import java.io.File;
import java.net.URL;

/**
* User: NachoSoto
* Date: 9/5/13
*/
public interface ZincJobCreator {
    ZincJob<ZincCatalog> downloadCatalog(final URL sourceURL);
    ZincJob<File> downloadArchive(final URL url, final File root, final String child);
}
