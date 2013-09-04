package zinc.classes;

import com.google.gson.Gson;
import zinc.classes.jobs.ZincJob;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * User: NachoSoto
 * Date: 9/3/13
 */
public class ZincRepo {
//    private static final String BUNDLES_DIR = "bundles";
//    private static final String CATALOGS_DIR = "catalogs";

    private final ExecutorService mExecutorService;
    private final ZincJobFactory mJobFactory;
    
    private final File mRoot;

    private final ZincRepoIndexWriter mIndexWriter;

    public ZincRepo(final ExecutorService executorService, final ZincJobFactory jobFactory, final Gson gson, final URI root) {
        mExecutorService = executorService;
        mJobFactory = jobFactory;
        mRoot = new File(root);
        mIndexWriter = new ZincRepoIndexWriter(mRoot, gson);
    }

    public void addSourceURL(final URL catalogURL, final String catalogIdentifier) {
        mIndexWriter.getIndex().addSourceURL(catalogURL, catalogIdentifier);

        downloadCatalog(catalogURL, catalogIdentifier);
        mIndexWriter.saveIndex();
    }

    private void downloadCatalog(final URL catalogURL, final String catalogIdentifier) {
        final Future<ZincCatalog> future = executeJob(mJobFactory.downloadCatalog(catalogURL, catalogIdentifier));

    }

    private <V> Future<V> executeJob(final ZincJob<V> job) {
        return mExecutorService.submit(job);
    }

    public static interface ZincJobFactory {
        ZincJob<ZincCatalog> downloadCatalog(final URL catalogURL, final String catalogIdentifier);
    }
}