package com.mindsnacks.zinc.classes.fileutils;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.mindsnacks.zinc.exceptions.ZincRuntimeException;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipException;

/**
 * User: NachoSoto
 * Date: 9/10/13
 */
public class FileHelper {
    private static final int BUFFER_SIZE = 8192;

    private final Gson mGson;
    private final HashUtil mHashUtil;

    public FileHelper(final Gson gson, final HashUtil hashUtil) {
        mGson = gson;
        mHashUtil = hashUtil;
    }

    public void unzipFile(final File originFolder,
                          final String originFilename,
                          final File destinationFolder,
                          final String destinationFilename,
                          final String expectedHash) throws IOException, ValidatingDigestOutputStream.HashFailedException {
        final File input = new File(originFolder, originFilename),
                   output = new File(destinationFolder, destinationFilename);

        if (!output.exists()) {
            createDirectories(output);

            final InputStream in;

            try {
                in = new BufferedInputStream(new GZIPInputStream(new FileInputStream(input)));
            } catch (ZipException e) {
                throw new ZincRuntimeException("Error opening gzip file: " + input.getAbsolutePath(), e);
            }

            final ValidatingDigestOutputStream digestStream = mHashUtil.wrapOutputStreamWithDigest(new FileOutputStream(output));
            final OutputStream dest = new BufferedOutputStream(digestStream);
            try {
                copy(in, dest);
            } finally {
                dest.close();
                in.close();
            }

            digestStream.validate(expectedHash);
        }
    }

    public Reader readerForFile(final File file) throws FileNotFoundException {
        return new BufferedReader(new FileReader(file));
    }

    public boolean moveFile(final File originFolder,
                            final String originFilename,
                            final File destinationFolder,
                            final String destinationFilename) {
        return moveFile(new File(originFolder, originFilename),
                new File(destinationFolder, destinationFilename));
    }

    public boolean moveFile(final File originFile,
                            final File destinationFile) {
        return originFile.renameTo(destinationFile);
    }

    public void copyFile(final File originFolder,
                         final String originFilename,
                         final File destinationFolder,
                         final String destinationFilename,
                         final String expectedHash) throws IOException, ValidatingDigestOutputStream.HashFailedException {
        final File input = new File(originFolder, originFilename),
                   output = new File(destinationFolder, destinationFilename);

        if (!output.exists()) {
            createDirectories(output);
            output.createNewFile();
            final ValidatingDigestOutputStream digestStream = mHashUtil.wrapOutputStreamWithDigest(new FileOutputStream(output));

            Files.copy(input, digestStream);

            digestStream.validate(expectedHash);
        }
    }

    /**
     * Deletes the file or directory denoted by this abstract pathname.
     * If this pathname denotes a directory, then the directory must be empty in order to be deleted.
     */
    public boolean removeFile(final File file) {
        return file.delete();
    }

    /**
     * Empties and removes a directory.
     */
    public boolean removeDirectory(final File directory) {
        return emptyDirectory(directory) && removeFile(directory);
    }

    /**
     * Removes all the files from a directory. Recursively.
     * @param folder directory to empty
     * @return true if all the files were correctly removed.
     */
    public boolean emptyDirectory(final File folder) {
        boolean result = true;

        if (folder.exists()) {
            for (final File file : folder.listFiles()) {
                if (file.isDirectory()) {
                    emptyDirectory(file);
                }
                result &= removeFile(file);
            }
        }

        return result;
    }

    public <V> V readJSON(final File file, final Class<V> vClass) throws FileNotFoundException {
        return mGson.fromJson(readerForFile(file), vClass);
    }

    public <V> void writeObject(final File file, final V object, final Class<V> vClass) throws IOException {
        createDirectories(file);

        final BufferedWriter writer = new BufferedWriter(new FileWriter(file));

        try {
            mGson.toJson(object, vClass, writer);
        } finally {
            writer.close();
        }
    }

    private boolean createDirectories(final File file) {
        return file.getParentFile().mkdirs();
    }

    public void streamToFile(final InputStream inputStream, final File file, final String expectedHash) throws IOException, ValidatingDigestOutputStream.HashFailedException {
        createDirectories(file);
        final ValidatingDigestOutputStream digestStream = mHashUtil.wrapOutputStreamWithDigest(new FileOutputStream(file));
        final OutputStream dest = new BufferedOutputStream(digestStream);
        try {
            copy(inputStream, dest);
        } finally {
            dest.close();
            inputStream.close();
        }

        digestStream.validate(expectedHash);
    }

    private void copy(InputStream inputStream, OutputStream outputStream) throws IOException {
        int read;
        final byte[] bytes = new byte[BUFFER_SIZE];
        while ((read = inputStream.read(bytes, 0, BUFFER_SIZE)) >= 0) {
            outputStream.write(bytes, 0, read);
        }
    }
}
