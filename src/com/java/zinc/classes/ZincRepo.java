package zinc.classes;

import com.google.gson.Gson;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.Executor;

/**
 * User: NachoSoto
 * Date: 9/3/13
 */
public class ZincRepo {
//    private static final String BUNDLES_DIR = "bundles";
//    private static final String CATALOGS_DIR = "catalogs";

    private final Executor mExecutor;
    
    private final File mRoot;

    private final ZincRepoIndexWriter mIndexWriter;
    
    public ZincRepo(final Executor executor, final Gson gson, final URI root) {
        mExecutor = executor;
        mRoot = new File(root);
        mIndexWriter = new ZincRepoIndexWriter(mRoot, gson);
    }

    public void addSourceURL(final URL sourceURL) {
        mIndexWriter.getIndex().addSourceURL(sourceURL);

        mIndexWriter.saveIndex();
    }
}