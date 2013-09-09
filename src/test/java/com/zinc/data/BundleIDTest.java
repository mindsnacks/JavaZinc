package com.zinc.data;

import com.zinc.classes.data.BundleID;
import com.zinc.utils.ZincBaseTest;
import org.junit.Test;

import static com.zinc.utils.MockFactory.randomString;
import static org.junit.Assert.assertEquals;

/**
 * User: NachoSoto
 * Date: 9/9/13
 */
public class BundleIDTest extends ZincBaseTest {
    @Test
    public void bundleNameIsTheSame() throws Exception {
        final String bundleName = "first-lesson";

        assertEquals(bundleName, new BundleID(randomString(), bundleName).getBundleName());
    }

    @Test
    public void catalogIDIsTheSame() throws Exception {
        final String catalogID = "com.mindsnacks.misc";

        assertEquals(catalogID, new BundleID(catalogID, randomString()).getCatalogID());
    }

    @Test
    public void bundleNameIsCorrectWhenInitializingWithBundleID() throws Exception {
        final String bundleName = "some-lesson",
                     bundleID = "com.mindsnacks.misc." + bundleName;

        assertEquals(bundleName, new BundleID(bundleID).getBundleName());
    }

    @Test
    public void catalogIDIsCorrectWhenInitializingWithBundleID() throws Exception {
        final String catalogID = "com.mindsnacks.lessons",
                     bundleID = catalogID + "." + randomString();

        assertEquals(catalogID, new BundleID(bundleID).getCatalogID());
    }
}
