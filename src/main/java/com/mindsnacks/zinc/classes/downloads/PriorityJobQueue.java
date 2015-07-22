package com.mindsnacks.zinc.classes.downloads;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.mindsnacks.zinc.exceptions.ZincRuntimeException;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * User: NachoSoto
 * Date: 9/21/13
 */
public class PriorityJobQueue<Input, Output> {
    private static final int INITIAL_QUEUE_CAPACITY = 20;
    private static final int SCHEDULER_TERMINATION_TIMEOUT = 30;
    private static final int EXECUTOR_SERVICE_TERMINATION_TIMEOUT = 300;
    private static final int FUTURE_WAITING_SECONDS_INTERVAL = 2;

    private final int mConcurrency;
    private final ThreadFactory mThreadFactory;
    private final DataProcessor<Input, Output> mDataProcessor;
    private final PriorityCalculator<Input> mPriorityCalculator;

    private ExecutorService mScheduler;
    private ListeningExecutorService mExecutorService;
    private ListeningExecutorService mFuturesExecutorService;

    private final SortablePriorityBlockingQueue<Input> mQueue;
    private final Map<Input, ListenableFuture<Output>> mFutures = new HashMap<Input, ListenableFuture<Output>>();
    private final Set<Input> mAddedElements = new HashSet<Input>();

    private final Lock mLock = new ReentrantLock();
    private final Condition mEnqueued = mLock.newCondition();
    private final Semaphore mEnqueuedDataSemaphore;
    private final AtomicBoolean mShouldReorder = new AtomicBoolean(false);

    public PriorityJobQueue(final int concurrency,
                            final ThreadFactory threadFactory,
                            final PriorityCalculator<Input> priorityCalculator,
                            final DataProcessor<Input, Output> dataProcessor) {
        mConcurrency = concurrency;
        mThreadFactory = threadFactory;
        mDataProcessor = dataProcessor;
        mEnqueuedDataSemaphore = new Semaphore(concurrency);

        mPriorityCalculator = priorityCalculator;
        mQueue = new SortablePriorityBlockingQueue<Input>(new PriorityBlockingQueue<Input>(INITIAL_QUEUE_CAPACITY, createPriorityComparator(mPriorityCalculator)));
    }

    public boolean isRunning() {
        return (mScheduler != null || mExecutorService != null);
    }

    public synchronized void start() {
        checkServiceIsRunning(false, "Service is already running");

        mScheduler = Executors.newSingleThreadExecutor(mThreadFactory);
        mFuturesExecutorService = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool(mThreadFactory));
        mExecutorService = MoreExecutors.listeningDecorator(new ThreadPoolExecutor(
                mConcurrency,
                mConcurrency,
                0L, TimeUnit.MICROSECONDS,
                new LinkedBlockingQueue<Runnable>(),
                mThreadFactory) {
            @Override
            protected void afterExecute(final Runnable r, final Throwable t) {
                super.afterExecute(r, t);

                mEnqueuedDataSemaphore.release();
            }
        });

        mScheduler.submit(createSchedulerTask());
    }

    private Comparator<Input> createPriorityComparator(final PriorityCalculator<Input> priorityCalculator) {
        final Comparator<DownloadPriority> comparator = DownloadPriority.createComparator();

        return new Comparator<Input>() {
            @Override
            public int compare(final Input o1, final Input o2) {
                return comparator.compare(
                        priorityCalculator.getPriorityForObject(o1),
                        priorityCalculator.getPriorityForObject(o2));
            }
        };
    }

    public synchronized boolean stop() throws InterruptedException {
        checkServiceIsRunning(true, "Service is already stopped");

        boolean stopped = false;
        mScheduler.shutdownNow();
        stopped = mScheduler.awaitTermination(SCHEDULER_TERMINATION_TIMEOUT, TimeUnit.SECONDS);

        mExecutorService.shutdown();
        stopped &= mExecutorService.awaitTermination(EXECUTOR_SERVICE_TERMINATION_TIMEOUT, TimeUnit.SECONDS);

        mFuturesExecutorService.shutdown();
        stopped &= mFuturesExecutorService.awaitTermination(EXECUTOR_SERVICE_TERMINATION_TIMEOUT, TimeUnit.SECONDS);

        if (stopped) {
            mScheduler = mExecutorService = mFuturesExecutorService = null;
        }

        return stopped;
    }

    public void add(final Input element) {
        if (!jobWasAdded(element)) {
            mLock.lock();

            try {
                mAddedElements.add(element);
                addElementToQueue(element);
            } finally {
                mLock.unlock();
            }
        }
    }

    public ListenableFuture<Output> get(final Input element) throws JobNotFoundException {
        checkServiceIsRunning(true, "Service should be running");
        checkJobWasAlreadyAdded(element);

        ListenableFuture<Output> result = findExistingFuture(element);

        if (didFutureFail(result)) {
            removeCachedFuture(element);
            addElementToQueue(element);
            result = null;
        }

        return (result != null) ? result : waitForFuture(element);
    }

    public void reAdd(final Input element) throws JobNotFoundException {
        checkServiceIsRunning(true, "Service should be running");
        checkJobWasAlreadyAdded(element);
        ListenableFuture<Output> result = findExistingFuture(element);
        removeCachedFuture(element);
        addElementToQueue(element);
    }

    private ListenableFuture<Output> findExistingFuture(final Input element) {
        mLock.lock();

        try {
            return mFutures.get(element);
        } finally {
            mLock.unlock();
        }
    }

    private void removeCachedFuture(final Input element) {
        mLock.lock();

        try {
            mFutures.remove(element);
        } finally {
            mLock.unlock();
        }
    }

    public static interface DataProcessor<Input, Output> {
        Callable<Output> process(Input data);
    }

    public void recalculatePriorities() {
        mShouldReorder.lazySet(true);
    }

    private Runnable createSchedulerTask() {
        return new Runnable() {
            public void run() {
                try {
                    Input data;
                    while ((data = mQueue.take()) != null) {
                        mEnqueuedDataSemaphore.acquire();

                        mLock.lock();

                        try {
                            mFutures.put(data, submit(data));
                            mEnqueued.signal();

                            if (mShouldReorder.getAndSet(false)) {
                                mQueue.reorder();
                            }
                        } finally {
                            mLock.unlock();
                        }
                    }
                } catch (final InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        };
    }

    private void checkServiceIsRunning(final boolean shouldBeRunning, final String errorMessage) {
        if (isRunning() != shouldBeRunning) {
            throw new ZincRuntimeException(errorMessage);
        }
    }

    private void checkJobWasAlreadyAdded(final Input element) {
        if (!jobWasAdded(element)) {
            throw new JobNotFoundException(element);
        }
    }

    private boolean jobWasAdded(final Input element) {
        return mAddedElements.contains(element);
    }

    private boolean didFutureFail(final Future<Output> future) {
        boolean didFail = false;

        if (future != null && future.isDone()) {
            try {
                future.get();
            } catch (Exception e) {
                didFail = true;
            }
        }

        return didFail;
    }

    private void addElementToQueue(final Input element) {
        mQueue.offer(element);
    }

    private ListenableFuture<Output> submit(final Input element) {
        return mExecutorService.submit(mDataProcessor.process(element));
    }

    private ListenableFuture<Output> waitForFuture(final Input element) {
        return Futures.dereference(mFuturesExecutorService.submit(new Callable<ListenableFuture<Output>>() {
            @Override
            public ListenableFuture<Output> call() throws Exception {
                ListenableFuture<Output> result;

                mLock.lock();

                try {
                    while ((result = mFutures.get(element)) == null) {
                        mEnqueued.await(FUTURE_WAITING_SECONDS_INTERVAL, TimeUnit.SECONDS);
                    }
                } finally {
                    mLock.unlock();
                }

                return result;
            }
        }));
    }

    public static class JobNotFoundException extends ZincRuntimeException {
        public JobNotFoundException(final Object object) {
            super((object == null) ? "Object is null" : "Object '" + object.toString() + "' had not been added");
        }
    }
}
