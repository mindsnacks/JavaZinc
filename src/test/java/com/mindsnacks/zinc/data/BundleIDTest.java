package com.mindsnacks.zinc.data;

import com.mindsnacks.zinc.classes.data.BundleID;
import com.mindsnacks.zinc.utils.ZincBaseTest;
import org.junit.Test;

import static com.mindsnacks.zinc.utils.TestFactory.randomString;
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
    public void testToString() throws Exception {
        final String catalogID = "com.mindsnacks.misc",
                     bundleName = "first-lesson";

        assertEquals(catalogID + "." + bundleName, new BundleID(catalogID, bundleName).toString());
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
