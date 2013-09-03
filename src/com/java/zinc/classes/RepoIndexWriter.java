package zinc.classes;

import com.google.gson.Gson;
import zinc.exceptions.ZincRuntimeException;

import java.io.*;

/**
 * User: NachoSoto
 * Date: 9/3/13
 */
public class RepoIndexWriter {
    private static final String REPO_INDEX_FILE = "repo.json";

    private final File mIndexFile;
    private final RepoIndex mRepoIndex;
    private final Gson mGson;

    public RepoIndexWriter(final File root, final Gson gson) {
        mGson = gson;
        mIndexFile = new File(root, REPO_INDEX_FILE);

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

    public void saveIndex() {
        try {
            final FileWriter fileWriter = new FileWriter(mIndexFile);
            mGson.toJson(mRepoIndex, fileWriter);
            fileWriter.close();

        } catch (IOException e) {
            throw new ZincRuntimeException("Error saving index", e);
        }
    }

    public RepoIndex getIndex() {
        return mRepoIndex;
    }
}
