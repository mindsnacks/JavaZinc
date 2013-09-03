package zinc.classes;

import com.google.gson.Gson;
import zinc.exceptions.ZincRuntimeException;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.Executor;

/**
 * User: NachoSoto
 * Date: 9/3/13
 */
public class Repo {
    private static final String REPO_INDEX_FILE = "repo.json";
    private static final String BUNDLES_DIR = "bundles";
    private static final String CATALOGS_DIR = "catalogs";

    private final Executor mExecutor;
    private final Gson mGson;
    
    private final File mRoot;
    private final File mIndexFile;

    private final RepoIndex mRepoIndex;
    
    public Repo(final Executor executor, final Gson gson, final URI root) {
        mExecutor = executor;
        mGson = gson;
        mRoot = new File(root);

        mIndexFile = new File(mRoot, REPO_INDEX_FILE);

        mRepoIndex = initializeIndex();
    }

    private RepoIndex initializeIndex() {
        try {
            final FileReader fileReader = new FileReader(mIndexFile);
            final RepoIndex result = mGson.fromJson(fileReader, RepoIndex.class);
            fileReader.close();

            return result;
        } catch (FileNotFoundException fnfe) {
            try {
                mIndexFile.createNewFile();

                return new RepoIndex();
            } catch (IOException ioe) {
                throw new ZincRuntimeException("Error creating index file", ioe);
            }
        } catch (IOException e) {
            throw new ZincRuntimeException("Error reading index file", e);
        }
    }

    private void saveIndex() {
        try {
            final FileWriter fileWriter = new FileWriter(mIndexFile);
            mGson.toJson(mRepoIndex, fileWriter);
            fileWriter.close();

        } catch (IOException e) {
            throw new ZincRuntimeException("Error saving index", e);
        }
    }

    public void addSourceURL(final URL sourceURL) {
        mRepoIndex.addSourceURL(sourceURL);

        saveIndex();
    }
}