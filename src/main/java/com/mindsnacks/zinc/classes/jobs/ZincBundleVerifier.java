package com.mindsnacks.zinc.classes.jobs;

import com.mindsnacks.zinc.classes.ZincLogging;
import com.mindsnacks.zinc.classes.data.ZincManifest;
import com.mindsnacks.zinc.classes.fileutils.HashUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;

/**
 * @author John Ericksen
 */
public class ZincBundleVerifier {

    public boolean shouldDownloadBundle(final File localBundleFolder, final ZincManifest manifest, final String flavorName) {
        if (!localBundleFolder.exists()) {
            return true;
        } else {
            File[] files = localBundleFolder.listFiles();
            if (files == null || files.length == 0) {
                return true;
            }
        }

        return validateBundleHashes(localBundleFolder, manifest, flavorName);
    }

    private boolean validateBundleHashes(final File localBundleFolder, final ZincManifest manifest, final String flavorName) {
        Map<String, ZincManifest.FileInfo> fileInfoMap = manifest.getFilesWithFlavor(flavorName);

        for (Map.Entry<String, ZincManifest.FileInfo> fileInfoEntry : fileInfoMap.entrySet()) {
            File bundlefile = new File(localBundleFolder, fileInfoEntry.getKey());
            if (!bundlefile.exists()) {
                return true;
            }
            try {
                String hash = HashUtil.sha1HashString(new FileInputStream(bundlefile));
                if (!hash.equals(fileInfoEntry.getValue().getHash())) {
                    return true;
                }
            } catch (FileNotFoundException e) {
                ZincLogging.log("ZincBundleVerifier", "Could not find file.");
                return true;
            }

        }
        return false;
    }
}
