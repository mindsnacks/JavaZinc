package com.mindsnacks.zinc.classes.data;
import java.util.concurrent.Future;

/**
 * Created by Miguel Carranza on 6/26/15.
 */
public interface ZincManifestsCache {
    boolean clearCachedManifests();

    Future<ZincManifest> getManifest(final SourceURL sourceURL,
                                     final String bundleName,
                                     final int version);
}
