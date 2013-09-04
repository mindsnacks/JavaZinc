package zinc.classes.jobs;

import java.io.*;
import java.net.URL;

/**
 * User: NachoSoto
 * Date: 9/4/13
 */
public class ZincDownloadFileJob extends AbstractZincDownloadFileJob {
    private static final int BUFFER_SIZE = 1024;

    public ZincDownloadFileJob(final ZincRequestExecutor requestExecutor, final URL url, final File root, final String child) {
        super(requestExecutor, url, root, child);
    }

    @Override
    protected void writeFile(final InputStreamReader inputStream, final File file) throws IOException {
        final FileWriter outputStream = new FileWriter(file);

        int read = 0;
        final char[] bytes = new char[BUFFER_SIZE];

        while ((read = inputStream.read(bytes, 0, BUFFER_SIZE)) != -1) {
            outputStream.write(bytes, 0, read);
        }

        outputStream.close();
    }
}
