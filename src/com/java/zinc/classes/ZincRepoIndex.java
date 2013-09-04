package zinc.classes;

import com.google.gson.annotations.SerializedName;
import zinc.exceptions.ZincRuntimeException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * User: NachoSoto
 * Date: 9/3/13
 */
public class ZincRepoIndex {
    @SerializedName("sources")
    final private Set<URL> mSources = new HashSet<URL>();

    @SerializedName("bundles")
    final private Map<String, TrackingInfo> mBundles = new HashMap<String, TrackingInfo>();

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

    public void trackBundle(final String bundleID, final String distribution) {
        mBundles.put(bundleID, new TrackingInfo(distribution));
    }

    @Override
    public String toString() {
        return "ZincRepoIndex{" +
                "mSources=" + mSources +
                '}';
    }

    public TrackingInfo getTrackingInfo(final String bundleID) {
        return mBundles.get(bundleID);
    }

    public static class TrackingInfo {
        @SerializedName("distribution")
        final private String mDistribution;

        public TrackingInfo(final String distribution) {
            mDistribution = distribution;
        }

        public String getDistribution() {
            return mDistribution;
        }
    }
}
