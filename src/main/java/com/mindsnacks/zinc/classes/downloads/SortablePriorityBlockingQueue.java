package com.mindsnacks.zinc.classes.downloads;

import com.google.common.util.concurrent.ForwardingBlockingQueue;
import com.mindsnacks.zinc.classes.ZincLogging;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * @author NachoSoto
 *
 * This class is not thread safe.
 */
public class SortablePriorityBlockingQueue<V> extends ForwardingBlockingQueue<V> {
    private final PriorityBlockingQueue<V> mQueue;

    public SortablePriorityBlockingQueue(final PriorityBlockingQueue<V> queue) {
        super();

        mQueue = queue;
    }

    @Override
    protected BlockingQueue<V> delegate() {
        return mQueue;
    }

    /**
     * Recalculates priorities for all the elements remaining in the queue
     */
    public void reorder() {
        final Collection<V> collection = new LinkedList<V>();

        final int i = mQueue.drainTo(collection);
        mQueue.addAll(collection);

        ZincLogging.log(getClass().getSimpleName(), "Reordered " + i + " elements");
    }
}
