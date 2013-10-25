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

    private static class Data {
        private final DownloadPriority mPriority;
        private final String mResult;

        public Data(final DownloadPriority priority, final String result) {
            mPriority = priority;
            mResult = result;
        }

        private DownloadPriority getPriority() {
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
            return 31 * mPriority.getValue() + (mResult != null ? mResult.hashCode() : 0);
        }

        @Override
        public String toString() {
            return "Data {" + mResult + ": " + mPriority + "}";
        }
    }

    private PriorityJobQueue<Data, String> queue;

    @Mock private PriorityJobQueue.DataProcessor<Data, String> mDataProcessor;
    @Mock private PriorityCalculator<Data> mPriorityCalculator;


    @Before
    public void setUp() throws Exception {
        queue = new PriorityJobQueue<Data, String>(
                CONCURRENCY,
                new TestFactory.DaemonThreadFactory(),
                mPriorityCalculator,
                mDataProcessor);

        when(mPriorityCalculator.getPriorityForObject(any(Data.class))).then(new Answer<Object>() {
            @Override
            public DownloadPriority answer(final InvocationOnMock invocationOnMock) throws Throwable {
                return ((Data)invocationOnMock.getArguments()[0]).getPriority();
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
        queue.add(new Data(DownloadPriority.NEEDED_SOON, "result"));
    }

    @Test
    public void dataResultCanBeRetrieved() throws Exception {
        final Data data = processAndAddRandomData();

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
    public void jobIsDoneReturnsTrueWhenFinished() throws Exception {
        final Data data = processAndAddRandomData();

        // run
        queue.start();
        final Future<String> result = queue.get(data);
        result.get();

        // verify
        assertTrue(result.isDone());
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

        // run
        assertNotNull(result);
        assertEquals(data.getResult(), result.get());

        // verify
        verify(mDataProcessor).process(data);
        verify(mPriorityCalculator, atLeast(1)).getPriorityForObject(data);
    }

    @Test
    public void dataIsProcessedInOrderOfPriority() throws Exception {
        final Data data = processAndAddRandomData();
        processAndAddRandomData();
        processAndAddRandomData();
        processAndAddRandomData();

        // run
        queue.start();
        queue.get(data);

        ArgumentCaptor<Data> argument = ArgumentCaptor.forClass(Data.class);
        verify(mDataProcessor, atLeast(1)).process(argument.capture());

        final List<Integer> priorities = Lists.transform(argument.getAllValues(), new Function<Data, Integer>() {
            @Override
            public Integer apply(final Data data) {
            return data.getPriority().getValue();
            }
        });

        assertTrue(Ordering.natural().reverse().isOrdered(priorities));
    }

    @Test(expected = PriorityJobQueue.JobNotFoundException.class)
    public void dataCannotBeRetrievedIfItWasNeverAdded() throws Exception {
        final Data data = randomData();

        queue.start();
        queue.get(data);
    }

    private Data randomData() {
        return new Data(randomPriority(), TestFactory.randomString());
    }

    private DownloadPriority randomPriority() {
        return DownloadPriority.values()[TestFactory.randomInt(0, DownloadPriority.values().length - 1)];
    }

    private Data processAndAddRandomData() {
        final Data data = randomData();

        processData(data);
        queue.add(data);

        return data;
    }

    private void processData(final Data data) {
        final Callable<String> processor = TestFactory.callableWithResult(data.getResult());
        when(mDataProcessor.process(data)).thenReturn(processor);
    }
}
