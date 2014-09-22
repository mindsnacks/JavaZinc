package com.mindsnacks.zinc.classes.jobs;

import com.mindsnacks.zinc.classes.ZincLogging;
import com.mindsnacks.zinc.classes.data.ZincManifest;
import com.mindsnacks.zinc.classes.fileutils.HashUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.Map;

/**
 * @author John Ericksen
 */
public class ZincBundleVerifier {

    private final HashUtil hashUtil;

    public ZincBundleVerifier(HashUtil hashUtil) {
        this.hashUtil = hashUtil;
    }

    public boolean verify(final File localBundleFolder, final ZincManifest manifest, final String flavorName) {
        boolean filesExist = true;
        if (!localBundleFolder.exists()) {
            filesExist = false;
        } else {
            File[] files = localBundleFolder.listFiles();
            if (files == null || files.length == 0) {
                filesExist = false;
            }
        }

        return filesExist && validateBundleHashes(localBundleFolder, manifest, flavorName);
    }

    private boolean validateBundleHashes(final File localBundleFolder, final ZincManifest manifest, final String flavorName) {
        boolean valid = true;
        Map<String, ZincManifest.FileInfo> fileInfoMap = manifest.getFilesWithFlavor(flavorName);

        Iterator<Map.Entry<String, ZincManifest.FileInfo>> mapEntryIter = fileInfoMap.entrySet().iterator();
        while (mapEntryIter.hasNext() && valid) {
            Map.Entry<String, ZincManifest.FileInfo> fileInfoEntry = mapEntryIter.next();
            File bundlefile = new File(localBundleFolder, fileInfoEntry.getKey());
            if (!bundlefile.exists()) {
                valid = false;
            }
            try {
                String hash = hashUtil.sha1HashString(new FileInputStream(bundlefile));
                if (!hash.equals(fileInfoEntry.getValue().getHash())) {
                    valid = false;
                }
            } catch (FileNotFoundException e) {
                ZincLogging.log("ZincBundleVerifier", "Could not find file " + bundlefile);
                valid = false;
            }
        }
        return valid;
    }
}
