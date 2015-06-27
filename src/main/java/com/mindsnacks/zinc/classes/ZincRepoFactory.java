package com.mindsnacks.zinc.classes;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mindsnacks.zinc.classes.data.*;
import com.mindsnacks.zinc.classes.downloads.DownloadPriority;
import com.mindsnacks.zinc.classes.downloads.PriorityCalculator;
import com.mindsnacks.zinc.classes.downloads.PriorityJobQueue;
import com.mindsnacks.zinc.classes.fileutils.FileHelper;
import com.mindsnacks.zinc.classes.fileutils.HashUtil;
import com.mindsnacks.zinc.classes.jobs.ZincDownloader;

import java.io.File;
import java.util.HashSet;
import java.util.Timer;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * User: NachoSoto
 * Date: 9/10/13
 */
public final class ZincRepoFactory {
    private static final int CATALOG_DOWNLOAD_THREAD_POOL_SIZE = 3;

    public Repo createRepo(final File root,
                           final String flavorName,
                           final int bundleCloneConcurrency,
                           final PriorityCalculator<BundleID> priorityCalculator) {
        final Gson gson = createGson();
        final ZincJobFactory jobFactory = createJobFactory(gson);
        final ZincRepoIndexWriter indexWriter = createRepoIndexWriter(root, gson);

        final ThreadFactory threadFactory = new DaemonThreadFactory();
        final ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(CATALOG_DOWNLOAD_THREAD_POOL_SIZE, threadFactory);

        final ZincCatalogsCache catalogs = createCatalogCache(jobFactory, root, gson, indexWriter.getIndex(), executorService);
        final ZincManifestCache manifests = createManifestCache(jobFactory, root, gson, executorService);

        final PriorityJobQueue<ZincCloneBundleRequest, ZincBundle> queue = createQueue(
                bundleCloneConcurrency,
                new ZincBundleDownloader(jobFactory, catalogs),
                createPriorityCalculator(priorityCalculator));

        return new ZincRepo(queue, root.toURI(), indexWriter, catalogs, flavorName);
    }

    private ZincCatalogsCache createCatalogCache(final ZincJobFactory jobFactory,
                                            final File root,
                                            final Gson gson,
                                            final ZincRepoIndex repoIndex,
                                            final ScheduledExecutorService executorService) {
        return new ZincCatalogs(root,
                    new FileHelper(gson, new HashUtil()),
                    new HashSet<SourceURL>(repoIndex.getSources()),
                    jobFactory,
                    executorService,
                    executorService,
                    new Timer(ZincCatalogs.class.getSimpleName(), true));
    }

    private ZincManifestCache createManifestCache(final ZincJobFactory jobFactory,
                                                  final File root,
                                                  final Gson gson,
                                                  final ScheduledExecutorService executorService) {
        return new ZincManifests(root,
                                 new FileHelper(gson, new HashUtil()),
                                 jobFactory,
                                 executorService,
                                 executorService);
    }

    private Gson createGson() {
        final GsonBuilder gsonBuilder = new GsonBuilder().serializeNulls().setVersion(1.0);
        gsonBuilder.registerTypeAdapter(SourceURL.class, new SourceURL.Serializer());
        gsonBuilder.registerTypeAdapter(SourceURL.class, new SourceURL.Deserializer());

        return gsonBuilder.create();
    }

    private ZincJobFactory createJobFactory(final Gson gson) {
        return new ZincDownloader(gson);
    }

    private ZincRepoIndexWriter createRepoIndexWriter(final File root, final Gson gson) {
        return new ZincRepoIndexWriter(root, gson);
    }

    private PriorityCalculator<ZincCloneBundleRequest> createPriorityCalculator(final PriorityCalculator<BundleID> priorityComparator) {
        return new PriorityCalculator<ZincCloneBundleRequest>() {
            @Override
            public DownloadPriority getPriorityForObject(final ZincCloneBundleRequest object) {
                return priorityComparator.getPriorityForObject(object.getBundleID());
            }
        };
    }

    private PriorityJobQueue<ZincCloneBundleRequest, ZincBundle> createQueue(final int bundleCloneConcurrency,
                                                                             final PriorityJobQueue.DataProcessor<ZincCloneBundleRequest, ZincBundle> bundleDownloader,
                                                                             final PriorityCalculator<ZincCloneBundleRequest> priorityComparator) {
        return new PriorityJobQueue<ZincCloneBundleRequest, ZincBundle>(
                bundleCloneConcurrency,
                new DaemonThreadFactory(),
                priorityComparator,
                bundleDownloader);
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
            namePrefix = "zinc-pool-" +
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
