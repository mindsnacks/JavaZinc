package com.zinc.classes;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zinc.classes.data.SourceURL;
import com.zinc.classes.data.ZincBundle;
import com.zinc.classes.data.ZincCloneBundleRequest;
import com.zinc.classes.downloads.PriorityJobQueue;
import com.zinc.classes.jobs.ZincDownloader;

import java.io.File;
import java.util.Comparator;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * User: NachoSoto
 * Date: 9/10/13
 */
public final class ZincRepoFactory {
    public ZincRepo createRepo(final File root,
                               final String flavorName,
                               final int bundleCloneConcurrency,
                               final ZincRepo.BundlePriorityComparator priorityComparator) {
        final Gson gson = createGson();
        final ZincJobFactory jobFactory = createJobFactory(gson);
        final ZincRepoIndexWriter indexWriter = createRepoIndexWriter(root, gson);

        final PriorityJobQueue<ZincCloneBundleRequest, ZincBundle> queue = createQueue(bundleCloneConcurrency, createBundleDownloader(jobFactory), createPriorityComparator(priorityComparator));

        return new ZincRepo(queue, root.toURI(), indexWriter, flavorName);
    }

    private PriorityJobQueue.DataProcessor<ZincCloneBundleRequest, ZincBundle> createBundleDownloader(final ZincJobFactory jobFactory) {
        return new PriorityJobQueue.DataProcessor<ZincCloneBundleRequest, ZincBundle>() {
            @Override
            public Callable<ZincBundle> process(final ZincCloneBundleRequest request) {
                return jobFactory.cloneBundle(request);
            }
        };
    }

    private Gson createGson() {
        // TODO: disable pretty printing
        final GsonBuilder gsonBuilder = new GsonBuilder().setPrettyPrinting().serializeNulls().setVersion(1.0);
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

    private Comparator<ZincCloneBundleRequest> createPriorityComparator(final ZincRepo.BundlePriorityComparator priorityComparator) {
        return new Comparator<ZincCloneBundleRequest>() {
            @Override
            public int compare(final ZincCloneBundleRequest o1, final ZincCloneBundleRequest o2) {
                return priorityComparator.compare(o1.getBundleID(), o2.getBundleID());
            }
        };
    }

    private PriorityJobQueue<ZincCloneBundleRequest, ZincBundle> createQueue(final int bundleCloneConcurrency,
                                                                             final PriorityJobQueue.DataProcessor<ZincCloneBundleRequest, ZincBundle> bundleDownloader,
                                                                             final Comparator<ZincCloneBundleRequest> priorityComparator) {
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
