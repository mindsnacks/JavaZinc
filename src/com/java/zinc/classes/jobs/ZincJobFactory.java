package zinc.classes.jobs;

import com.google.gson.Gson;
import zinc.classes.ZincCatalog;
import zinc.classes.ZincRepo;
import zinc.exceptions.ZincRuntimeException;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * User: NachoSoto
 * Date: 9/3/13
 */
public class ZincJobFactory implements ZincRepo.ZincJobFactory {
    private final Gson mGson;

    public ZincJobFactory(final Gson gson) {
        mGson = gson;
    }

    @Override
    public ZincJob<ZincCatalog> downloadCatalog(final URL sourceURL, final String catalogID) {
        final URL url;
        try {
            url = new URL(sourceURL, catalogID);
        } catch (MalformedURLException e) {
            throw new ZincRuntimeException("Invalid URL", e);
        }

        return new ZincDownloadFileJob.JobFactory<ZincCatalog>().createJob(url, mGson, ZincCatalog.class);
    }
}
