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
    private final Gson mGson;

    private RepoIndex mRepoIndex;
    private FileWriter mFileWriter;

    public RepoIndexWriter(final File root, final Gson gson) {
        mGson = gson;
        mIndexFile = new File(root, REPO_INDEX_FILE);
    }

    private RepoIndex initializeIndex() {
        try {
            return mGson.fromJson(new FileReader(mIndexFile), RepoIndex.class);
        } catch (FileNotFoundException fnfe) {
            try {
                mIndexFile.createNewFile();

                return new RepoIndex();
            } catch (IOException ioe) {
                throw new ZincRuntimeException("Error creating index file", ioe);
            }
        }
    }

    private FileWriter getFileWriter() {
        if (mFileWriter == null) {
            try {
                mFileWriter = new FileWriter(mIndexFile);
            } catch (IOException e) {
                throw new ZincRuntimeException("Cannot write to index file", e);
            }
        }

        return mFileWriter;
    }

    public void saveIndex() {
        final FileWriter fileWriter = getFileWriter();

        mGson.toJson(mRepoIndex, fileWriter);

        try {
            fileWriter.flush();
        } catch (IOException e) {
            throw new ZincRuntimeException("Error writing to index file", e);
        }
    }

    public RepoIndex getIndex() {
        if (mRepoIndex == null) {
            mRepoIndex = initializeIndex();
        }

        return mRepoIndex;
    }
}
