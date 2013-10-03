package com.mindsnacks.zinc.classes.downloads;

import com.google.common.base.Function;
import com.google.common.util.concurrent.Futures;
import com.mindsnacks.zinc.exceptions.ZincRuntimeException;

import java.util.*;
import java.util.concurrent.*;
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

    private final int mConcurrency;
    private final ThreadFactory mThreadFactory;
    private final DataProcessor<Input, Output> mDataProcessor;

    private ExecutorService mScheduler;
    private ExecutorService mExecutorService;
    private ExecutorService mFuturesExecutorService;

    private final PriorityBlockingQueue<Input> mQueue;
    private final Map<Input, Future<Output>> mFutures = new HashMap<Input, Future<Output>>();
    private final Set<Input> mAddedElements = new HashSet<Input>();

    private final Lock mLock = new ReentrantLock();
    private final Condition mEnqueued = mLock.newCondition();
    private final Semaphore mEnqueuedDataSemahore;

    public PriorityJobQueue(final int concurrency,
                            final ThreadFactory threadFactory,
                            final PriorityCalculator<Input> priorityCalculator,
                            final DataProcessor<Input, Output> dataProcessor) {
        mConcurrency = concurrency;
        mThreadFactory = threadFactory;
        mDataProcessor = dataProcessor;
        mEnqueuedDataSemahore = new Semaphore(concurrency);

        mQueue = new PriorityBlockingQueue<Input>(INITIAL_QUEUE_CAPACITY, createPriorityComparator(priorityCalculator));
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

    public boolean isRunning() {
        return (mScheduler != null || mExecutorService != null);
    }

    public synchronized void start() {
        checkServiceIsRunning(false, "Service is already running");

        mScheduler = Executors.newSingleThreadExecutor(mThreadFactory);
        mFuturesExecutorService = Executors.newCachedThreadPool(mThreadFactory);
        mExecutorService = new ThreadPoolExecutor(
                mConcurrency,
                mConcurrency,
                0L, TimeUnit.MICROSECONDS,
                new LinkedBlockingQueue<Runnable>(),
                mThreadFactory) {
            @Override
            protected void afterExecute(final Runnable r, final Throwable t) {
                super.afterExecute(r, t);

                mEnqueuedDataSemahore.release();
            }
        };

        mScheduler.submit(createSchedulerTask());
    }

    private Runnable createSchedulerTask() {
        return new Runnable() {
            public void run() {
                try {
                    Input data;
                    while ((data = mQueue.take()) != null) {
                        mEnqueuedDataSemahore.acquire();

                        mLock.lock();

                        try {
                            mFutures.put(data, submit(data));
                            mEnqueued.signal();
                        } finally {
                            mLock.unlock();
                        }
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
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

    private void checkServiceIsRunning(final boolean shouldBeRunning, final String errorMessage) {
        if (isRunning() != shouldBeRunning) {
            throw new ZincRuntimeException(errorMessage);
        }
    }

    public void add(final Input element) {
        mAddedElements.add(element);
        mQueue.put(element);
    }

    public Future<Output> get(final Input element) throws JobNotFoundException {
        if (mAddedElements.contains(element)) {
            return waitForFuture(element);
        } else {
            throw new JobNotFoundException(element);
        }
    }

    private Future<Output> submit(final Input element) {
        return mExecutorService.submit(mDataProcessor.process(element));
    }

    private Future<Output> waitForFuture(final Input element) {
        return Futures.lazyTransform(mFuturesExecutorService.submit(new Callable<Future<Output>>() {
            @Override
            public Future<Output> call() throws Exception {
                Future<Output> result;

                mLock.lock();

                try {
                    while ((result = mFutures.get(element)) == null) {
                        mEnqueued.awaitUninterruptibly();
                    }
                } finally {
                    mLock.unlock();
                }

                return result;
            }
        }), new Function<Future<Output>, Output>() {
            @Override
            public Output apply(final Future<Output> future) {
                try {
                    return future.get();
                } catch (Exception e) {
                    throw new ZincRuntimeException("Error waiting for future", e);
                }
            }
        });
    }

    public static interface DataProcessor<Input, Output> {
        Callable<Output> process(Input data);
    }

    public static class JobNotFoundException extends ZincRuntimeException {
        public JobNotFoundException(final Object object) {
            super((object == null) ? "Object is null" : "Object '" + object.toString() + "' had not been added");
        }
    }
}
