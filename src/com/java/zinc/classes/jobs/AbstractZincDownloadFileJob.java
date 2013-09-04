package zinc.classes.jobs;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * User: NachoSoto
 * Date: 9/4/13
 */
public abstract class AbstractZincDownloadFileJob extends AbstractZincDownloadJob<File> {
    private final File mFile;

    public AbstractZincDownloadFileJob(final ZincRequestExecutor requestExecutor, final URL url, final File root, final String child) {
        super(requestExecutor, url, File.class);
        mFile = new File(root, child);
    }

    @Override
    public File call() throws DownloadFileError {
        final InputStreamReader inputStream = mRequestExecutor.get(mUrl);

        try {
            writeFile(inputStream, mFile);
        } catch (IOException e) {
            throw new DownloadFileError("Error writing to file '" + mFile.getAbsolutePath() + "'", e);
        }

        return mFile;
    }

    abstract protected void writeFile(final InputStreamReader inputStream, final File file) throws IOException;
}
