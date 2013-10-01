package com.mindsnacks.zinc.classes.jobs;

import com.mindsnacks.zinc.classes.ZincLogging;

import java.util.concurrent.Callable;

/**
 * User: NachoSoto
 * Date: 9/3/13
 */
public abstract class ZincJob<V> implements Callable<V> {
    protected String getJobName() {
        return this.getClass().getSimpleName();
    }

    protected final void logMessage(final String message) {
        ZincLogging.log(getJobName(), message);
    }

    @Override
    public final V call() throws Exception {
        logMessage("started");
        final V result = run();
        logMessage("finished");

        return result;
    }

    protected abstract V run() throws Exception;
}