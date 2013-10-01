package com.mindsnacks.zinc.classes;

import com.google.gson.Gson;
import com.mindsnacks.zinc.classes.data.ZincRepoIndex;
import com.mindsnacks.zinc.exceptions.ZincRuntimeException;

import java.io.*;

/**
 * User: NachoSoto
 * Date: 9/3/13
 */
public class ZincRepoIndexWriter {
    private static final String REPO_INDEX_FILE = "repo.json";

    private final File mIndexFile;
    private final Gson mGson;

    private ZincRepoIndex mRepoIndex;

    public ZincRepoIndexWriter(final File root, final Gson gson) {
        mGson = gson;
        mIndexFile = new File(root, REPO_INDEX_FILE);
    }

    public void saveIndex() {
        final Writer fileWriter = getFileWriter();

        mGson.toJson(mRepoIndex, fileWriter);

        try {
            fileWriter.close();
        } catch (IOException e) {
            throw new ZincRuntimeException("Error writing to index file", e);
        }
    }

    public ZincRepoIndex getIndex() {
        if (mRepoIndex == null) {
            mRepoIndex = initializeIndex();
        }

        return mRepoIndex;
    }

    private ZincRepoIndex initializeIndex() {
        try {
            return mGson.fromJson(new FileReader(mIndexFile), ZincRepoIndex.class);
        } catch (FileNotFoundException fnfe) {
            try {
                mIndexFile.getParentFile().mkdirs();
                mIndexFile.createNewFile();

                return new ZincRepoIndex();
            } catch (IOException ioe) {
                throw new ZincRuntimeException("Error creating index file", ioe);
            }
        }
    }

    private Writer getFileWriter() {
        try {
            return new BufferedWriter(new FileWriter(mIndexFile));
        } catch (IOException e) {
            throw new ZincRuntimeException("Cannot write to index file", e);
        }
    }
}
