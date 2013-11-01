package com.mindsnacks.zinc.classes.data;

import java.util.concurrent.Future;

/**
 * @author NachoSoto
 */
public interface ZincCatalogsCache {
    boolean clearCachedCatalogs();
    void scheduleUpdate();

    Future<ZincCatalog> getCatalog(final SourceURL sourceURL);
}
