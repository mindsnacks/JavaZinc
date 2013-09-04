package zinc.classes.jobs;

import com.google.gson.Gson;

import java.io.InputStreamReader;
import java.net.URL;

/**
 * User: NachoSoto
 * Date: 9/4/13
 */
public class ZincDownloadObjectJob<V> extends AbstractZincDownloadJob<V> {
    private final Gson mGson;

    public ZincDownloadObjectJob(final ZincRequestExecutor requestFactory, final URL url, final Gson gson, final Class<V> theClass) {
        super(requestFactory, url, theClass);
        mGson = gson;
    }

    @Override
    public V call() throws Exception {
        return mGson.fromJson(new InputStreamReader(mRequestExecutor.get(mUrl)), mClass);
    }
}
