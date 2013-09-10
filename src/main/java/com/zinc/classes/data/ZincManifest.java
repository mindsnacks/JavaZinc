package com.zinc.classes.data;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * User: NachoSoto
 * Date: 9/10/13
 */
public class ZincManifest {
    @SerializedName("flavors")
    private final List<String> mFlavors;

    @SerializedName("catalog")
    private final String mIdentifier;

    @SerializedName("version")
    private final int mVersion;

    @SerializedName("bundle")
    private final String mBundleName;

    @SerializedName("files")
    private final Map<String, FileInfo> mFiles;

    public ZincManifest(final List<String> flavors,
                        final String identifier,
                        final int version,
                        final String bundleName, final Map<String, FileInfo> files) {
        mFlavors = flavors;
        mIdentifier = identifier;
        mVersion = version;
        mBundleName = bundleName;
        mFiles = files;
    }

    public List<String> getFlavors() {
        return mFlavors;
    }

    public String getIdentifier() {
        return mIdentifier;
    }

    public int getVersion() {
        return mVersion;
    }

    public String getBundleName() {
        return mBundleName;
    }

    /**
     * @return Map (filename => hash)
     */
    public Map<String,String> getFilesWithFlavor(final String flavor) {
          final Map<String, String> result = new HashMap<String, String>();

        for (final Map.Entry<String, FileInfo> entry : mFiles.entrySet()) {
            final FileInfo fileInfo = entry.getValue();
            if (fileInfo.getFlavors().contains(flavor)) {
                result.put(entry.getKey(), fileInfo.getHash());
            }
        }

        return result;
    }

    public class FileInfo {
        @SerializedName("flavors")
        private final Set<String> mFlavors;

        @SerializedName("sha")
        private final String mHash;

        public FileInfo(final Set<String> flavors, final String hash) {
            mFlavors = flavors;
            mHash = hash;
        }

        public Set<String> getFlavors() {
            return mFlavors;
        }

        public String getHash() {
            return mHash;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            return mHash.equals(((FileInfo) o).mHash);
        }

        @Override
        public int hashCode() {
            return mHash.hashCode();
        }
    }
}