package com.zinc.downloads;

import com.zinc.classes.downloads.DownloadPriority;
import com.zinc.classes.downloads.DownloadPriorityCalculator;
import com.zinc.classes.downloads.PriorityCalculator;
import com.zinc.utils.ZincBaseTest;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.*;

/**
 * User: NachoSoto
 * Date: 9/27/13
 */
public class DownloadPriorityCalculatorTest extends ZincBaseTest {
    private DownloadPriorityCalculator<Data> calculator;

    @Before
    public void setUp() throws Exception {
        calculator = new DownloadPriorityCalculator<Data>();
    }

    @Test
    public void priorityIsUnknownIfThereAreNoHandlers() throws Exception {
        assertEquals(DownloadPriority.UNKNOWN, calculator.getPriorityForObject(new Data()));
    }

    @Test
    public void priorityWithOnlyOneHandler() throws Exception {
        final DownloadPriority expectedResult = DownloadPriority.NEEDED_SOON;
        final Data object = new Data();

        final PriorityCalculator<Data> handler = calculatorWithPriority(expectedResult);

        // run
        calculator.addHandler(handler);
        final DownloadPriority result = calculator.getPriorityForObject(object);

        // verify
        assertEquals(expectedResult, result);
        verify(handler).getPriorityForObject(object);
    }

    @Test
    public void priorityWithTwoHandlersReturnsHigher() throws Exception {
        final DownloadPriority expectedResult = DownloadPriority.NEEDED_IMMEDIATELY;

        // run
        calculator.addHandler(calculatorWithPriority(expectedResult));
        calculator.addHandler(calculatorWithPriority(DownloadPriority.NEEDED_SOON));

        // verify
        assertEquals(expectedResult, calculator.getPriorityForObject(new Data()));
    }

    @SuppressWarnings("unchecked")
    private PriorityCalculator<Data> calculatorWithPriority(final DownloadPriority priority) {
        final PriorityCalculator result = mock(PriorityCalculator.class);

        doReturn(priority).when(result).getPriorityForObject(anyObject());

        return result;
    }

    private static class Data {

    }
}
