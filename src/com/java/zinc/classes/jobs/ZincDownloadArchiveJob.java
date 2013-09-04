package zinc.classes.jobs;

import com.ice.tar.TarInputStream;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * User: NachoSoto
 * Date: 9/4/13
 */
public class ZincDownloadArchiveJob extends AbstractZincDownloadFileJob {
    public ZincDownloadArchiveJob(final ZincRequestExecutor requestExecutor, final URL url, final File root, final String child) {
        super(requestExecutor, url, root, child);
    }

    @Override
    protected void writeFile(final InputStreamReader inputStream, final File file) throws IOException {
        final TarInputStream tar = new TarInputStream(inputStream);
    }


}
