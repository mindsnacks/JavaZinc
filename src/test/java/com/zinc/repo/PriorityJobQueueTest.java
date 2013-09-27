package com.zinc.repo;

import com.zinc.classes.downloads.DownloadPriority;
import com.zinc.classes.downloads.PriorityCalculator;
import com.zinc.classes.downloads.PriorityJobQueue;
import com.zinc.exceptions.ZincRuntimeException;
import com.zinc.utils.MockFactory;
import com.zinc.utils.ZincBaseTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

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

    private static class Data {
        private final int mPriority;
        private final String mResult;

        public Data(final int priority, final String result) {
            mPriority = priority;
            mResult = result;
        }

        private int getPriority() {
            return mPriority;
        }

        private String getResult() {
            return mResult;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final Data data = (Data) o;

            return (mPriority == data.mPriority) && !(mResult != null ? !mResult.equals(data.mResult) : data.mResult != null);

        }

        @Override
        public int hashCode() {
            return 31 * mPriority + (mResult != null ? mResult.hashCode() : 0);
        }
    }

    private PriorityJobQueue<Data, String> queue;

    @Mock private PriorityJobQueue.DataProcessor<Data, String> mDataProcessor;
    @Mock private PriorityCalculator<Data> mPriorityCalculator;


    @Before
    public void setUp() throws Exception {
        queue = new PriorityJobQueue<Data, String>(
                CONCURRENCY,
                new MockFactory.DaemonThreadFactory(),
                mPriorityCalculator,
                mDataProcessor);

        doReturn(DownloadPriority.UNKNOWN).when(mPriorityCalculator).getPriorityForObject(any(Data.class));
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
        queue.add(new Data(1, "result"));
    }

    @Test
    public void dataResultCanBeRetrieved() throws Exception {
        final Data data = processAndAddRandomData();

        // run
        queue.start();
        final Future<String> result = queue.get(data);

        verify(mDataProcessor).process(data);
        assertNotNull(result);
        assertEquals(data.getResult(), result.get());
    }

    @Test
    public void dataResultCanBeRetrievedIfOtherObjectsWereAddedBefore() throws Exception {
        final Data data = processAndAddRandomData();

        processAndAddRandomData();
        processAndAddRandomData();
        processAndAddRandomData();

        // run
        queue.start();
        final Future<String> result = queue.get(data);

        verify(mDataProcessor).process(data);
        verify(mPriorityCalculator, atLeast(1)).getPriorityForObject(data);
        assertNotNull(result);
        assertEquals(data.getResult(), result.get());
    }

    @Test(expected = PriorityJobQueue.JobNotFoundException.class)
    public void dataCannotBeRetrievedIfItWasNeverAdded() throws Exception {
        final Data data = randomData();

        queue.start();
        queue.get(data);
    }

    private Data randomData() {
        return new Data(MockFactory.randomInt(1, 10), MockFactory.randomString());
    }

    private Data processAndAddRandomData() {
        final Data data = randomData();

        processData(data);
        queue.add(data);

        return data;
    }

    private void processData(final Data data) {
        final Callable<String> processor = MockFactory.callableWithResult(data.getResult());
        when(mDataProcessor.process(data)).thenReturn(processor);
    }
}
