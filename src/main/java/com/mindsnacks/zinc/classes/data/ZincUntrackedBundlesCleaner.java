package com.mindsnacks.zinc.classes.data;

import com.mindsnacks.zinc.classes.fileutils.FileHelper;

import java.io.File;

/**
 * Created by Miguel Carranza on 7/2/15.
 */
public class ZincUntrackedBundlesCleaner {
    private final FileHelper mFileHelper;

    public ZincUntrackedBundlesCleaner(final FileHelper fileHelper) {
        mFileHelper = fileHelper;
    }

    public void cleanBundle(final File repoFolder,
                            final BundleID bundleID) {
        final File bundlesFolder = new File(repoFolder, PathHelper.getBundlesFolder());
        final File manifestsFolder = new File(repoFolder, PathHelper.getManifestsFolder(bundleID.getCatalogID()));

        cleanBundles(bundlesFolder, bundleID.toString());
        cleanAllManifests(manifestsFolder, bundleID.getBundleName());
    }

    public void cleanUntrackedBundles(final File repoFolder,
                                      final BundleID bundleID,
                                      final int currentVersion) {
        final File bundlesFolder = new File(repoFolder, PathHelper.getBundlesFolder());
        final File manifestsFolder = new File(repoFolder, PathHelper.getManifestsFolder(bundleID.getCatalogID()));

        cleanBundles(bundlesFolder, bundleID.toString());
        cleanManifests(manifestsFolder, bundleID.getBundleName(), currentVersion);
    }

    private void cleanBundles(final File bundlesFolder,
                              final String bundleID) {
        if (bundlesFolder.exists()) {
            for (File bundleFolder : bundlesFolder.listFiles()) {
                if (PathHelper.getBundleID(bundleFolder.getName()).equals(bundleID)) {
                    mFileHelper.removeDirectory(bundleFolder);
                }
            }
        }
    }

    private void cleanManifests(final File manifestsFolder,
                                final String bundleName,
                                final int currentVersion) {
        if (manifestsFolder.exists()) {
            for (File manifestFile : manifestsFolder.listFiles()) {
                if (PathHelper.getBundleName(manifestFile.getName()).equals(bundleName) &&
                        PathHelper.getBundleVersion(manifestFile.getName()) != currentVersion) {
                    mFileHelper.removeFile(manifestFile);
                }
            }
        }
    }

    private void cleanAllManifests(final File manifestsFolder,
                                   final String bundleName) {
        if (manifestsFolder.exists()) {
            for (File manifestFile : manifestsFolder.listFiles()) {
                if (PathHelper.getBundleName(manifestFile.getName()).equals(bundleName)) {
                    mFileHelper.removeFile(manifestFile);
                }
            }
        }
    }
}
