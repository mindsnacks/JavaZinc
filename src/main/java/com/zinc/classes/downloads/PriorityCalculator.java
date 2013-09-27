package com.zinc.classes.downloads;

/**
 * User: NachoSoto
 * Date: 9/27/13
 */
public interface PriorityCalculator <V> {
    public DownloadPriority getPriorityForObject(final V object);
}
