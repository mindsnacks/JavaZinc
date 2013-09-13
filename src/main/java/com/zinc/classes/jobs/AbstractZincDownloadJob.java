package com.zinc.classes.jobs;

import com.zinc.exceptions.ZincRuntimeException;

import java.net.URL;

/**
 * User: NachoSoto
 * Date: 9/3/13
 */
public abstract class AbstractZincDownloadJob<V> extends ZincJob<V> {
    protected final ZincRequestExecutor mRequestExecutor;
    protected final URL mUrl;
    protected final Class<V> mClass;

    public AbstractZincDownloadJob(final ZincRequestExecutor requestExecutor, final URL url, final Class<V> theClass) {
        mRequestExecutor = requestExecutor;
        mUrl = url;
        mClass = theClass;
    }

    public static class DownloadFileError extends ZincRuntimeException {
        public DownloadFileError(final String message) {
            super(message);
        }

        public DownloadFileError(final String message, final Throwable cause) {
            super(message, cause);
        }
    }

    @Override
    protected String getJobName() {
        return super.getJobName() + " (" + mUrl +")";
    }
}
