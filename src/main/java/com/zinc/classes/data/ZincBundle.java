package com.zinc.classes.data;

import java.io.File;

/**
 * User: NachoSoto
 * Date: 9/4/13
 */
public class ZincBundle extends File {
    final private BundleID mBundleID;

    public ZincBundle(final String root, final BundleID bundleID) {
        super(root, bundleID.toString());

        mBundleID = bundleID;
    }

    public ZincBundle(final File file, final BundleID bundleID) {
        super(file.getPath());

        mBundleID = bundleID;
    }

    public BundleID getBundleID() {
        return mBundleID;
    }
}
