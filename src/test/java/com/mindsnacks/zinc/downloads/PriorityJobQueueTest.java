package com.mindsnacks.zinc.downloads;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.mindsnacks.zinc.classes.downloads.DownloadPriority;
import com.mindsnacks.zinc.classes.downloads.PriorityCalculator;
import com.mindsnacks.zinc.classes.downloads.PriorityJobQueue;
import com.mindsnacks.zinc.exceptions.ZincRuntimeException;
import com.mindsnacks.zinc.utils.TestFactory;
import com.mindsnacks.zinc.utils.ZincBaseTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * User: NachoSoto
 * Date: 9/25/13
 */
public class PriorityJobQueueTest extends ZincBaseTest {

    public static final int CONCURRENCY = 1;

    private PriorityJobQueue<TestData, String> queue;

    @Mock private PriorityJobQueue.DataProcessor<TestData, String> mDataProcessor;
    @Mock private PriorityCalculator<TestData> mPriorityCalculator;

    @Before
    public void setUp() throws Exception {
        queue = new PriorityJobQueue<TestData, String>(
                CONCURRENCY,
                new TestFactory.DaemonThreadFactory(),
                mPriorityCalculator,
                mDataProcessor);

        when(mPriorityCalculator.getPriorityForObject(any(TestData.class))).then(new Answer<Object>() {
            @Override
            public DownloadPriority answer(final InvocationOnMock invocationOnMock) throws Throwable {
                return ((TestData) invocationOnMock.getArguments()[0]).getPriority();
            }
        });
    }

    @Test
    public void notRunningByDefault() throws Exception {
        assertFalse(queue.isRunning());
    }

    @Test
    public void canBeStarted() throws Exception {
        queue.start();

        assertTrue(queue.isRunning());
    }

    @Test(expected = ZincRuntimeException.class)
    public void cannotBeStartedTwice() throws Exception {
        queue.start();
        queue.start();
    }

    @Test
    public void canBeStopped() throws Exception {
        queue.start();
        assertTrue(queue.stop());
    }

    @Test
    public void notRunningAfterBeingStopped() throws Exception {
        queue.start();
        queue.stop();

        assertFalse(queue.isRunning());
    }

    @Test(expected = ZincRuntimeException.class)
    public void cannotBeStoppedIfNotStarted() throws Exception {
        queue.stop();
    }

    @Test
    public void dataCanBeAdded() throws Exception {
        queue.add(new TestData(DownloadPriority.NEEDED_SOON, "result"));
    }

    @Test
    public void dataResultCanBeRetrieved() throws Exception {
        final TestData data = processAndAddRandomData();

        // run
        queue.start();
        final Future<String> result = queue.get(data);

        // run
        assertNotNull(result);
        assertEquals(data.getResult(), result.get());

        // verify
        verify(mDataProcessor).process(data);
    }

    @Test
    public void dataResultCanBeRetrievedIfOtherObjectsWereAddedBefore() throws Exception {
        final TestData data = processAndAddRandomData();

        processAndAddRandomData();
        processAndAddRandomData();
        processAndAddRandomData();

        // run
        queue.start();
        final Future<String> result = queue.get(data);

        // run
        assertNotNull(result);
        assertEquals(data.getResult(), result.get());

        // verify
        verify(mDataProcessor).process(data);
        verify(mPriorityCalculator, atLeast(1)).getPriorityForObject(data);
    }

    @Test
    public void dataIsProcessedInOrderOfPriority() throws Exception {
        final TestData data = processAndAddRandomData();
        processAndAddRandomData();
        processAndAddRandomData();
        processAndAddRandomData();

        // run
        queue.start();
        queue.get(data);

        ArgumentCaptor<TestData> argument = ArgumentCaptor.forClass(TestData.class);
        verify(mDataProcessor, atLeast(1)).process(argument.capture());

        final List<Integer> priorities = Lists.transform(argument.getAllValues(), new Function<TestData, Integer>() {
            @Override
            public Integer apply(final TestData data) {
            return data.getPriority().getValue();
            }
        });

        assertTrue(Ordering.natural().reverse().isOrdered(priorities));
    }

    @Test(expected = PriorityJobQueue.JobNotFoundException.class)
    public void dataCannotBeRetrievedIfItWasNeverAdded() throws Exception {
        final TestData data = TestData.randomTestData();

        queue.start();
        queue.get(data);
    }

    private TestData processAndAddRandomData() {
        final TestData data = TestData.randomTestData();

        processData(data);
        queue.add(data);

        return data;
    }

    private void processData(final TestData data) {
        final Callable<String> processor = TestFactory.callableWithResult(data.getResult());
        when(mDataProcessor.process(data)).thenReturn(processor);
    }
}
