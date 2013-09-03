package zinc.classes;

import com.google.gson.annotations.SerializedName;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * User: NachoSoto
 * Date: 9/3/13
 */
public class RepoIndex {
    @SerializedName("sources")
    final private Set<URL> mSources = new HashSet<URL>();

    public Set<URL> getSources() {
        return mSources;
    }

    public void addSourceURL(URL url) {
        mSources.add(url);
    }

    @Override
    public String toString() {
        return "RepoIndex{" +
                "mSources=" + mSources +
                '}';
    }
}
