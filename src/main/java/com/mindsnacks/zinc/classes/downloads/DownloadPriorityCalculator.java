package com.mindsnacks.zinc.classes.downloads;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * User: NachoSoto
 * Date: 9/27/13
 */
public class DownloadPriorityCalculator<V> extends PriorityCalculator<V> {
    private final Set<PriorityCalculator<V>> mHandlers = new HashSet<PriorityCalculator<V>>();

    public DownloadPriority getPriorityForObject(final V object) {
        if (mHandlers.size() > 0) {
            return Collections.max(Collections2.transform(mHandlers, new Function<PriorityCalculator<V>, DownloadPriority>() {
                @Override
                public DownloadPriority apply(final PriorityCalculator<V> handler) {
                    return handler.getPriorityForObject(object);
                }
            }));
        } else {
            return DownloadPriority.UNKNOWN;
        }
    }

    public void addHandler(final PriorityCalculator<V> handler) {
        mHandlers.add(handler);
    }
}
