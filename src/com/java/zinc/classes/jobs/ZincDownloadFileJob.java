package zinc.classes.jobs;

import java.io.*;
import java.net.URL;

/**
 * User: NachoSoto
 * Date: 9/4/13
 */
public class ZincDownloadFileJob extends AbstractZincDownloadJob<File> {
    private static final int BUFFER_SIZE = 1024;

    private final File mFile;

    public ZincDownloadFileJob(final ZincRequestExecutor requestExecutor, final URL url, final File root, final String filename) {
        super(requestExecutor, url, File.class);
        mFile = new File(root, filename);
    }

    @Override
    public File call() throws DownloadFileError, IOException {
        final InputStreamReader inputStream = mRequestExecutor.get(mUrl);
        FileWriter outputStream = null;

        try {
            outputStream = new FileWriter(mFile);

            int read = 0;
            char[] bytes = new char[BUFFER_SIZE];

            while ((read = inputStream.read(bytes, 0, BUFFER_SIZE)) != -1) {
                outputStream.write(bytes, 0, read);
            }
        } catch (IOException e) {
            throw new DownloadFileError("Error writing to file '" + mFile.getAbsolutePath() + "'", e);
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
        }

        return mFile;
    }
}
