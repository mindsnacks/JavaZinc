package com.mindsnacks.zinc.classes.fileutils;

import com.mindsnacks.zinc.classes.data.ZincManifest;
import com.pegasus.data.accounts.PegasusAccountFieldValidator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Map;

/**
 * Created by Miguel Carranza on 6/28/15.
 */
public class BundleIntegrityVerifier {
    public static boolean isLocalBundleValid(final File localBundleFolder,
                                             final ZincManifest manifest,
                                             final String flavorName) {
        boolean isValid = true;
        final Map<String, ZincManifest.FileInfo> files = manifest.getFilesWithFlavor(flavorName);

        for (final Map.Entry<String, ZincManifest.FileInfo> entry : files.entrySet()) {
            final String fileName = entry.getKey();
            final ZincManifest.FileInfo fileInfo = entry.getValue();
            final File localFile = new File(localBundleFolder, fileName);
            final String expectedHash = fileInfo.getHash();
            isValid = isValid && isLocalFileValid(localFile, expectedHash);
        }

        return isValid;
    }

    private static boolean isLocalFileValid(final File localFile,
                                            final String expectedHash) {
        boolean isValid = false;
        final HashUtil hashUtil = new HashUtil();

        try {
            final ValidatingDigestInputStream digestStream = hashUtil.wrapInputStreamWithDigest(new FileInputStream(localFile));
            digestStream.validate(expectedHash);
            isValid = true;
        } catch (FileNotFoundException e) {
        } catch (ValidatingDigestInputStream.HashFailedException e) {}

        return isValid;
    }
}