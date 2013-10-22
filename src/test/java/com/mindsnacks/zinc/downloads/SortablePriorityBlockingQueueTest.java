package com.mindsnacks.zinc.downloads;

import com.mindsnacks.zinc.classes.downloads.SortablePriorityBlockingQueue;
import com.mindsnacks.zinc.utils.ZincBaseTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.PriorityBlockingQueue;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * @author NachoSoto
 */
public class SortablePriorityBlockingQueueTest extends ZincBaseTest {
    private SortablePriorityBlockingQueue<TestData> queue;
    private @Mock PriorityBlockingQueue<TestData> mBlockingQueue;

    private final TestData mData = TestData.randomTestData();

    @Before
    public void setUp() throws Exception {
        queue = new SortablePriorityBlockingQueue<TestData>(mBlockingQueue);
    }

    @Test
    public void getsElementFromQueue() throws Exception {
        doReturn(mData).when(mBlockingQueue).take();
        assertEquals(mData, queue.take());
    }

    @Test
    public void putsElementInQueue() throws Exception {
        queue.put(mData);

        verify(mBlockingQueue, times(1)).put(mData);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void reorder() throws Exception {
        final Collection<TestData> objects = Arrays.asList(
                TestData.randomTestData(),
                TestData.randomTestData(),
                TestData.randomTestData());

        doAnswer(new Answer() {
            @Override
            public Object answer(final InvocationOnMock invocationOnMock) throws Throwable {
                final Collection<TestData> collection = (Collection<TestData>)invocationOnMock.getArguments()[0];
                collection.addAll(objects);
                return null;
            }
        }).when(mBlockingQueue).drainTo(anyCollectionOf(TestData.class));

        // run
        queue.reorder();

        // verify
        verify(mBlockingQueue).addAll(eq(objects));
    }
}
