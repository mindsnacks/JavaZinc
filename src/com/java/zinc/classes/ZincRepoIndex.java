package zinc.classes;

import com.google.gson.annotations.SerializedName;
import zinc.exceptions.ZincRuntimeException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * User: NachoSoto
 * Date: 9/3/13
 */
public class ZincRepoIndex {
    @SerializedName("sources")
    final private Set<URL> mSources = new HashSet<URL>();

    public Set<URL> getSources() {
        return mSources;
    }

    public void addSourceURL(final URL catalogURL, final String catalogIdentifier) {
        try {
            addSourceURL(new URL(catalogURL, catalogIdentifier));
        } catch (MalformedURLException e) {
            throw new ZincRuntimeException("Invalid url (catalogURL: " + catalogURL + ", catalogID: " + catalogIdentifier, e);
        }
    }

    public void addSourceURL(final URL sourceURL) {
        mSources.add(sourceURL);
    }

    @Override
    public String toString() {
        return "ZincRepoIndex{" +
                "mSources=" + mSources +
                '}';
    }
}
