package zinc.classes.jobs;

import com.github.kevinsawicki.http.HttpRequest;
import com.google.gson.Gson;
import zinc.classes.ZincCatalog;
import zinc.classes.ZincRepo;
import zinc.exceptions.ZincRuntimeException;

import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * User: NachoSoto
 * Date: 9/3/13
 */
public class ZincJobFactory implements ZincRepo.ZincJobFactory {
    private static final String CATALOG_FILENAME = "catalog.json";

    private final Gson mGson;

    public ZincJobFactory(final Gson gson) {
        mGson = gson;
    }

    @Override
    public AbstractZincJob<ZincCatalog> downloadCatalog(final URL sourceURL, final String catalogID) {
        final URL url;
        try {
            url = new URL(sourceURL, catalogID + "/" + CATALOG_FILENAME);
        } catch (MalformedURLException e) {
            throw new ZincRuntimeException("Invalid URL", e);
        }

        return new ZincDownloadObjectJob<ZincCatalog>(createRequestExecutor(), url, mGson, ZincCatalog.class);
    }

    private ZincRequestExecutor createRequestExecutor() {
        return new ZincRequestExecutor() {
            @Override
            public InputStreamReader get(final URL url) throws AbstractZincDownloadJob.DownloadFileError {
                try {
                    return getRequest(url).reader();
                } catch (HttpRequest.HttpRequestException e) {
                    throw new AbstractZincDownloadJob.DownloadFileError("Error downloading file at url '" + url + "'", e);
                }
            }

            private HttpRequest getRequest(final URL url) {
                return HttpRequest.get(url).acceptGzipEncoding().uncompress(true);
            }
        };
    }
}
