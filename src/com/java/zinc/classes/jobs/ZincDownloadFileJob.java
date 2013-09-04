package zinc.classes.jobs;

import java.io.File;
import java.net.URL;

/**
 * User: NachoSoto
 * Date: 9/4/13
 */
public class ZincDownloadFileJob extends ZincDownloadJob<File> {
    private final File mPath;

    public ZincDownloadFileJob(final ZincRequestExecutor requestExecutor, final URL url, final File path) {
        super(requestExecutor, url, File.class);
        mPath = path;
    }

    @Override
    public File call() throws DownloadFileError {
        if (!mPath.exists()) {
            if (!mPath.mkdir()) {
                throw new DownloadFileError("Error creating folder: " + mPath.getAbsolutePath());
            }
        }

        return null;
    }
}
