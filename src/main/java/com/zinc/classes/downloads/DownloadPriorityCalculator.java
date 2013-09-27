package com.zinc.classes.downloads;

import java.util.HashSet;
import java.util.Set;

/**
 * User: NachoSoto
 * Date: 9/27/13
 */
public class DownloadPriorityCalculator<V> implements PriorityCalculator<V> {
    private final Set<Handler<V>> mHandlers = new HashSet<Handler<V>>();

    public DownloadPriority getPriorityForObject(final V object) {
        DownloadPriority result = DownloadPriority.UNKNOWN;

        for (final Handler<V> handler : mHandlers) {
            result = result.getMaxPriority(handler.getPriorityForObject(object));
        }

        return result;
    }

    public void addHandler(final Handler<V> handler) {
        mHandlers.add(handler);
    }

    public static interface Handler<V> {
        DownloadPriority getPriorityForObject(V object);
    }
}
