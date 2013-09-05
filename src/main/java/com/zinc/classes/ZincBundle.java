package com.zinc.classes;

import java.io.File;

/**
 * User: NachoSoto
 * Date: 9/4/13
 */
public class ZincBundle extends File {
    final private String mBundleID;

    public ZincBundle(final String root, final String bundleID) {
        super(root, bundleID);

        mBundleID = bundleID;
    }

    public String getBundleID() {
        return mBundleID;
    }
}
