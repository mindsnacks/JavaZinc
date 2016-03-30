package com.mindsnacks.zinc.classes.data;

import com.google.gson.annotations.SerializedName;
import com.mindsnacks.zinc.classes.fileutils.FileHelper;
import com.mindsnacks.zinc.exceptions.ZincRuntimeException;

import java.io.File;
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

    transient private Map<String, Map<String, FileInfo>> mFilesForFlavor;

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

    private Map<String, Map<String, FileInfo>> getFilesMap() {
        if (mFilesForFlavor == null) {
            mFilesForFlavor = new HashMap<String, Map<String, FileInfo>>();
        }

        return mFilesForFlavor;
    }

    /**
     * @return Map (filename => FileInfo)
     */
    public Map<String, FileInfo> getFilesWithFlavor(final String flavor) {
        final Map<String, Map<String, FileInfo>> filesMap = getFilesMap();

        if (filesMap.get(flavor) == null) {
            final Map<String, FileInfo> result = new HashMap<String, FileInfo>();

            for (final Map.Entry<String, FileInfo> entry : mFiles.entrySet()) {
                final String filename = entry.getKey();
                final FileInfo fileInfo = entry.getValue();

                if (fileInfo.getFlavors().contains(flavor)) {
                    result.put(filename, fileInfo);
                }
            }

            filesMap.put(flavor, result);
        }

        return filesMap.get(flavor);
    }

    public boolean archiveExists(final String flavor) {
        /**
         * Archives are only created for bundles with at least 2 files
         */
        return (getFilesWithFlavor(flavor).size() > 1);
    }

    public boolean containsFiles(final String flavor) {
        return (!getFilesWithFlavor(flavor).isEmpty());
    }

    /**
     * This method can only be used if !archiveExists() && containsFiles
     * to obtain the single file in a bundle.
     */
    public FileInfo getFileWithFlavor(final String flavor) {
        return getFilesWithFlavor(flavor).get(getFilenameWithFlavor(flavor));
    }

    /**
     * This method can only be used if !archiveExists() && containsFiles
     * to obtain the single filename in a bundle.
     */
    public String getFilenameWithFlavor(final String flavor) {
        final Map<String, FileInfo> files = getFilesWithFlavor(flavor);

        if (archiveExists(flavor) || !containsFiles(flavor)) {
            throw new ZincRuntimeException(String.format("This manifest has %d files for flavor '%s'", files.size(), flavor));
        }

        return files.keySet().iterator().next();
    }

    public static class FileInfo {
        @SerializedName("flavors")
        private final Set<String> mFlavors;

        @SerializedName("sha")
        private final String mHash;

        @SerializedName("formats")
        private final Map<String, Map<String, Integer>> mFormats;

        public FileInfo() {
            mFlavors = null;
            mHash = null;
            mFormats = null;
        }

        public FileInfo(final Set<String> flavors, final String hash, final Map<String, Map<String, Integer>> formats) {
            mFlavors = flavors;
            mHash = hash;
            mFormats = formats;
        }

        public Set<String> getFlavors() {
            return mFlavors;
        }

        public String getHash() {
            return mHash;
        }

        /**
         * @return relative path for file in the repo.
         */
        public String getFilePath() {
            // sha[0:2]/sha[2:4]/sha.extension
            return mHash.substring(0, 2) + File.separator + mHash.substring(2, 4) + File.separator + getHashWithExtension();
        }

        public String getHashWithExtension() {
            if (isGzipped()) {
                return getHash() + "." + FileHelper.GZIPPED_FORMAT;
            } else {
                return getHash();
            }
        }

        public boolean isGzipped() {
            return mFormats.containsKey(FileHelper.GZIPPED_FORMAT);
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
