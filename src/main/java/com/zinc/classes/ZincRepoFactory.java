package com.zinc.classes;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zinc.classes.data.SourceURL;
import com.zinc.classes.jobs.ZincDownloader;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * User: NachoSoto
 * Date: 9/10/13
 */
public final class ZincRepoFactory {
    public ZincRepo createRepo(final File root) {
        // TODO: disable pretty printing
        final GsonBuilder gsonBuilder = new GsonBuilder().setPrettyPrinting().serializeNulls().setVersion(1.0);
        gsonBuilder.registerTypeAdapter(SourceURL.class, new SourceURL.Serializer());
        gsonBuilder.registerTypeAdapter(SourceURL.class, new SourceURL.Deserializer());

        final Gson gson = gsonBuilder.create();

        final ExecutorService executorService = Executors.newCachedThreadPool();

        final ZincFutureFactory jobFactory = new ZincDownloader(gson, executorService);
        final ZincRepoIndexWriter indexWriter = new ZincRepoIndexWriter(root, gson);

        return new ZincRepo(jobFactory, root.toURI(), indexWriter);
    }
}
