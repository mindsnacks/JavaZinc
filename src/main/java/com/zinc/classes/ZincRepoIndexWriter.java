package com.zinc.classes;

import com.google.gson.Gson;
import com.zinc.exceptions.ZincRuntimeException;

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
    private FileWriter mFileWriter;

    public ZincRepoIndexWriter(final File root, final Gson gson) {
        mGson = gson;
        mIndexFile = new File(root, REPO_INDEX_FILE);
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
                mIndexFile.createNewFile();

                return new ZincRepoIndex();
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
}
