package com.mindsnacks.zinc.classes.fileutils;

import com.mindsnacks.zinc.classes.data.ZincManifest;
import com.pegasus.data.accounts.PegasusAccountFieldValidator;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Miguel Carranza on 6/28/15.
 */
public class BundleIntegrityVerifier {
    private static final int BUFFER_SIZE = 8192;

    public static boolean isLocalBundleValid(final File localBundleFolder,
                                             final ZincManifest manifest,
                                             final String flavorName) {
        boolean isValid = true;
        final Map<String, ZincManifest.FileInfo> files = manifest.getFilesWithFlavor(flavorName);
        final Iterator<String> it = files.keySet().iterator();

        while(it.hasNext() && isValid) {
            final String fileName = it.next();
            final ZincManifest.FileInfo fileInfo = files.get(fileName);
            final String expectedHash = fileInfo.getHash();
            final File localFile = new File(localBundleFolder, fileName);

            isValid &= isLocalFileValid(localFile, expectedHash);
        }

        return isValid;
    }

    private static boolean isLocalFileValid(final File localFile,
                                            final String expectedHash) {
        boolean isValid = false;
        final HashUtil hashUtil = new HashUtil();

        try {
            final ValidatingDigestInputStream digestStream = hashUtil.wrapInputStreamWithDigest(new FileInputStream(localFile));
            final InputStream in = new BufferedInputStream(digestStream);
            readInputStream(in);
            in.close();
            digestStream.validate(expectedHash);
            isValid = true;
        } catch (FileNotFoundException e) {
        } catch (ValidatingDigestInputStream.HashFailedException e) {
        } catch (IOException e) {}

        return isValid;
    }

    private static void readInputStream(final InputStream stream) throws IOException {
        final byte[] bytes = new byte[BUFFER_SIZE];
        for (int read = 0; (read = stream.read(bytes, 0, BUFFER_SIZE)) !=-1;) {}
    }
}