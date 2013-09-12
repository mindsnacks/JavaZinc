package com.zinc.classes.jobs;

import com.zinc.classes.ZincLogging;

import java.util.concurrent.Callable;

/**
 * User: NachoSoto
 * Date: 9/3/13
 */
public abstract class ZincJob<V> implements Callable<V> {
    protected String getJobName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public final V call() throws Exception {
        final String className = getJobName();

        ZincLogging.log(String.format("%s started", className));
        final V result = run();
        ZincLogging.log(String.format("%s finished", className));

        return result;
    }

    protected abstract V run() throws Exception;
}