package com.mindsnacks.zinc.classes.downloads;

/**
 * User: NachoSoto
 * Date: 9/27/13
 */
public abstract class PriorityCalculator <V> {
    public abstract DownloadPriority getPriorityForObject(final V object);

    private Runnable mUpdatePriorities;

    public void updatePriorities() {
        mUpdatePriorities.run();
    }

    public void setUpdatePrioritiesRunnable(final Runnable runnable) {
        mUpdatePriorities = runnable;
    }
}
