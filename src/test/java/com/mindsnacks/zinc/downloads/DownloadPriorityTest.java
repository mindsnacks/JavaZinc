package com.mindsnacks.zinc.downloads;

import com.mindsnacks.zinc.classes.downloads.DownloadPriority;
import com.mindsnacks.zinc.utils.ZincBaseTest;
import org.junit.Test;

import java.util.Comparator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

/**
 * User: NachoSoto
 * Date: 9/27/13
 */
public class DownloadPriorityTest extends ZincBaseTest {
    @Test
    public void notNeededDoesNotEqualUnknown() throws Exception {
        assertEquals(DownloadPriority.UNKNOWN, DownloadPriority.NOT_NEEDED);
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

    @Test
    public void comparatorReturns0IfEqual() throws Exception {
        final Comparator<DownloadPriority> comparator = DownloadPriority.createComparator();

        assertEquals(0, comparator.compare(DownloadPriority.NOT_NEEDED, DownloadPriority.NOT_NEEDED));
    }

    @Test
    public void comparatorReturns1IfLowerPriority() throws Exception {
        final Comparator<DownloadPriority> comparator = DownloadPriority.createComparator();

        assertEquals(1, comparator.compare(DownloadPriority.NEEDED_SOON, DownloadPriority.NEEDED_IMMEDIATELY));
    }

    @Test
    public void comparatorReturnsMinus1IfGreaterPriority() throws Exception {
        final Comparator<DownloadPriority> comparator = DownloadPriority.createComparator();

        assertEquals(-1, comparator.compare(DownloadPriority.NEEDED_SOON, DownloadPriority.NOT_NEEDED));
    }
}
