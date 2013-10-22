package com.mindsnacks.zinc.classes.downloads;

import com.google.common.util.concurrent.ForwardingBlockingQueue;
import com.mindsnacks.zinc.classes.ZincLogging;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author NachoSoto
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
    public synchronized void reorder() {
        final Collection<V> collection = new LinkedList<V>();

        final int i = mQueue.drainTo(collection);
        mQueue.addAll(collection);

        ZincLogging.log(getClass().getSimpleName(), "Reordered " + i + " elements");
    }

    /* -------------------- synchronized methods -------------- */

    @Override
    public synchronized V take() throws InterruptedException {
        return super.take();
    }

    @Override
    public synchronized void put(final V v) throws InterruptedException {
        super.put(v);
    }

    @Override
    public synchronized boolean offer(final V o) {
        return super.offer(o);
    }

    @Override
    public synchronized int remainingCapacity() {
        return super.remainingCapacity();
    }

    @Override
    public synchronized V peek() {
        return super.peek();
    }

    @Override
    public synchronized V element() {
        return super.element();
    }

    @Override
    public synchronized V poll() {
        return super.poll();
    }

    @Override
    public synchronized V remove() {
        return super.remove();
    }

    @Override
    public synchronized V poll(final long timeout, final TimeUnit unit) throws InterruptedException {
        return super.poll(timeout, unit);
    }

    @Override
    public synchronized boolean offer(final V v, final long timeout, final TimeUnit unit) throws InterruptedException {
        return super.offer(v, timeout, unit);
    }

    @Override
    public synchronized int drainTo(final Collection<? super V> c) {
        return super.drainTo(c);
    }

    @Override
    public synchronized int drainTo(final Collection<? super V> c, final int maxElements) {
        return super.drainTo(c, maxElements);
    }
}
