package com.zinc.classes;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zinc.classes.data.SourceURL;
import com.zinc.classes.jobs.ZincDownloader;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * User: NachoSoto
 * Date: 9/10/13
 */
public final class ZincRepoFactory {
    public ZincRepo createRepo(final File root, final String flavorName) {
        // TODO: disable pretty printing
        final GsonBuilder gsonBuilder = new GsonBuilder().setPrettyPrinting().serializeNulls().setVersion(1.0);
        gsonBuilder.registerTypeAdapter(SourceURL.class, new SourceURL.Serializer());
        gsonBuilder.registerTypeAdapter(SourceURL.class, new SourceURL.Deserializer());

        final Gson gson = gsonBuilder.create();

        final ExecutorService executorService = Executors.newCachedThreadPool(new DaemonThreadFactory());

        final ZincFutureFactory jobFactory = new ZincDownloader(gson, executorService);
        final ZincRepoIndexWriter indexWriter = new ZincRepoIndexWriter(root, gson);

        return new ZincRepo(jobFactory, root.toURI(), indexWriter, flavorName);
    }

    // extracted from Executors.DefaultThreadFactory (which unfortunately is private)
    private static class DaemonThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        DaemonThreadFactory() {
            final SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();
            namePrefix = "pool-" +
                    poolNumber.getAndIncrement() +
                    "-thread-";
        }

        @Override
        public Thread newThread(Runnable r) {
            final Thread t = new Thread(group, r,
                    namePrefix + threadNumber.getAndIncrement(),
                    0);
            t.setDaemon(true);

            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);

            return t;
        }
    }
}
