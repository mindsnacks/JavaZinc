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

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
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

    private final List<TestData> mAddedData = new LinkedList<TestData>();

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
        
        mAddedData.clear();
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
        queue.add(TestData.randomTestData());
    }

    @Test
    public void dataIsNotProcessedIfItWasAlreadyAdded() throws Exception {
        final TestData data = processAndAddRandomData();

        queue.start();

        processAndAddData(data);

        waitForDataToBeProcessed();
        verifyDataWasProcessedOnce(data);
    }

    @Test
    public void dataCannotBeAddedTwiceEvenAfterFinishing() throws Exception {
        final TestData data = processAndAddRandomData();

        // run
        queue.start();

        waitForDataToBeProcessed();
        queue.add(data);

        waitForDataToBeProcessed();
        verifyDataWasProcessedOnce(data);
    }

    @Test(expected = ZincRuntimeException.class)
    public void dataCannotBeRetrievedIfStopped() throws Exception {
        final TestData data = TestData.randomTestData();

        queue.add(data);
        queue.get(data);
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
    }

    @Test
    public void dataIsProcessed() throws Exception {
        final TestData data = processAndAddRandomData();

        // run
        queue.start();
        queue.get(data).get();

        // verify
        verifyDataWasProcessedOnce(data);
    }

    @Test
    public void dataIsNotProcessedMultipleTimesIfRetrievedMultipleTimes() throws Exception {
        final TestData data = processAndAddRandomData();

        // run
        queue.start();

        queue.get(data).get();
        queue.get(data).get();

        // verify
        verifyDataWasProcessedOnce(data);
    }

    @Test(expected = ExecutionException.class)
    public void dataReturnsErrorIfFailed() throws Exception {
        final TestData data = processWithErrorAndAddRandomData();

        // run
        queue.start();
        queue.get(data).get();
    }

    @Test
    public void dataIsProcessedAgainIfItFailed() throws Exception {
        final TestData data = processWithErrorAndAddRandomData();

        // run
        queue.start();

        // wait for future to finish
        try {
            queue.get(data).get();
        } catch (ExecutionException e) {}

        // process again with a positive result this time
        processData(data);

        // run
        final String result = queue.get(data).get();

        // verify
        verify(mDataProcessor, times(2)).process(data);

        assertNotNull(result);
        assertEquals(data.getResult(), result);
    }

    @Test
    public void jobIsDoneReturnsTrueWhenFinished() throws Exception {
        final TestData data = processAndAddRandomData();

        // run
        queue.start();
        final Future<String> result = queue.get(data);
        result.get();

        // verify
        assertTrue(result.isDone());
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
        verifyDataWasProcessedOnce(data);
        verify(mPriorityCalculator, atLeast(1)).getPriorityForObject(data);
    }

    @Test
    public void dataIsProcessedInOrderOfPriority() throws Exception {
        processAndAddData(TestData.randomTestData(DownloadPriority.UNKNOWN));
        processAndAddData(TestData.randomTestData(DownloadPriority.NEEDED_VERY_SOON));
        processAndAddData(TestData.randomTestData(DownloadPriority.NEEDED_SOON));
        processAndAddData(TestData.randomTestData(DownloadPriority.NEEDED_SOON));
        processAndAddData(TestData.randomTestData(DownloadPriority.NEEDED_IMMEDIATELY));

        // run
        queue.start();

        waitForDataToBeProcessed();

        // verify
        assertDataWasProcessedInOrder();
    }

    @Test
    public void dataIsProcessedInOrderOfPriorityAfterChangingPriorities() throws Exception {
        final TestData lowPriority = processAndAddData(TestData.randomTestData(DownloadPriority.UNKNOWN));

        processAndAddData(TestData.randomTestData(DownloadPriority.NEEDED_IMMEDIATELY));
        processAndAddData(TestData.randomTestData(DownloadPriority.NEEDED_IMMEDIATELY));
        processAndAddData(TestData.randomTestData(DownloadPriority.NEEDED_SOON));
        processAndAddData(TestData.randomTestData(DownloadPriority.UNKNOWN));
        processAndAddData(TestData.randomTestData(DownloadPriority.NEEDED_VERY_SOON));

        // change priority
        lowPriority.setPriority(DownloadPriority.NEEDED_IMMEDIATELY);

        // run
        queue.start();
        queue.recalculatePriorities();

        waitForDataToBeProcessed();

        // verify
        assertDataWasProcessedInOrder();
    }

    @Test(expected = PriorityJobQueue.JobNotFoundException.class)
    public void dataCannotBeRetrievedIfItWasNeverAdded() throws Exception {
        final TestData data = TestData.randomTestData();

        queue.start();
        queue.get(data);
    }

    @Test(expected = PriorityJobQueue.JobNotFoundException.class)
    public void dataIsNotReaddedIfNotAlreadyThere() throws Exception {
        final TestData data = TestData.randomTestData();

        queue.start();
        queue.reAdd(data);
    }

    private TestData processAndAddRandomData() {
        return processAndAddData(TestData.randomTestData());
    }

    private TestData processWithErrorAndAddRandomData() {
        return processWithErrorAndAddData(TestData.randomTestData());
    }

    private TestData processAndAddData(final TestData data) {
        processData(data);
        queue.add(data);

        return data;
    }

    private TestData processWithErrorAndAddData(final TestData data) {
        processDataWithError(data);
        queue.add(data);

        return data;
    }

    private void processData(final TestData data) {
        _processData(data, TestFactory.callableWithResult(data.getResult()));
    }

    private void processDataWithError(final TestData data) {
        _processData(data, TestFactory.<String>callableWithError(new JobFailedException()));
    }

    private void _processData(final TestData data, final Callable<String> processor) {
        when(mDataProcessor.process(data)).thenReturn(processor);
        mAddedData.add(data);
    }

    private void waitForDataToBeProcessed() throws ExecutionException, InterruptedException {
        for (final TestData data : mAddedData) {
            try {
                queue.get(data).get();
            } catch (PriorityJobQueue.JobNotFoundException e) {
                // Ignore. Data might have already been retrieved.
            }
        }
    }

    private void assertDataWasProcessedInOrder() {
        final ArgumentCaptor<TestData> argument = ArgumentCaptor.forClass(TestData.class);
        verify(mDataProcessor, times(mAddedData.size())).process(argument.capture());

        final List<Integer> priorities = Lists.transform(argument.getAllValues(), new Function<TestData, Integer>() {
            @Override
            public Integer apply(final TestData data) {
                return data.getPriority().getValue();
            }
        });

        assertTrue(Ordering.natural().reverse().isOrdered(priorities));
    }

    private void verifyDataWasProcessedOnce(final TestData data) {
        verify(mDataProcessor, times(1)).process(data);
    }

    private class JobFailedException extends ZincRuntimeException {
        public JobFailedException() {
            super("job failed");
        }
    }
}
