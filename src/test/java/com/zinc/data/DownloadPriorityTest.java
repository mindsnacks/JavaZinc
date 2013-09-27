package com.zinc.data;

import com.zinc.classes.downloads.DownloadPriority;
import com.zinc.utils.ZincBaseTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

/**
 * User: NachoSoto
 * Date: 9/27/13
 */
public class DownloadPriorityTest extends ZincBaseTest {
    @Test
    public void notNeededDoesNotEqualUnknown() throws Exception {
        assertNotSame(DownloadPriority.UNKNOWN, DownloadPriority.NOT_NEEDED);
    }

    @Test
    public void notNeededAndUnknownHaveTheSameValue() throws Exception {
        assertEquals(DownloadPriority.UNKNOWN.getValue(), DownloadPriority.NOT_NEEDED.getValue());
    }

    @Test
    public void getMaxPriorityWithLowerPriority() throws Exception {
        assertEquals(DownloadPriority.NEEDED_SOON, DownloadPriority.NEEDED_SOON.getMaxPriority(DownloadPriority.NOT_NEEDED));
    }

    @Test
    public void getMaxPriorityWithHigherPriority() throws Exception {
        assertEquals(DownloadPriority.NEEDED_IMMEDIATELY, DownloadPriority.NEEDED_SOON.getMaxPriority(DownloadPriority.NEEDED_IMMEDIATELY));
    }
}
