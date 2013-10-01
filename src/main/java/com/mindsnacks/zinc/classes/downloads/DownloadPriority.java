package com.mindsnacks.zinc.classes.downloads;

import java.util.Comparator;

/**
 * User: NachoSoto
 * Date: 9/25/13
 */
public enum DownloadPriority {
    UNKNOWN(0),
    NOT_NEEDED(0),
    NEEDED_SOON(1),
    NEEDED_VERY_SOON(2),
    NEEDED_IMMEDIATELY(3);

    private final int mValue;

    private DownloadPriority(final int value) {
        mValue = value;
    }

    public int getValue() {
        return mValue;
    }

    @Override
    public String toString() {
        switch (this) {
            case NOT_NEEDED: return "Not needed";
            case NEEDED_SOON: return "Needed soon";
            case NEEDED_VERY_SOON: return "Needed very soon";
            case NEEDED_IMMEDIATELY: return "Needed immediately";
            case UNKNOWN: default: return "Unknown";
        }
    }

    /**
     * @return MAX(this, priority)
     */
    public DownloadPriority getMaxPriority(final DownloadPriority priority) {
        return (priority.getValue() > getValue()) ? priority : this;
    }

    public static Comparator<DownloadPriority> createComparator() {
        return new Comparator<DownloadPriority>() {
            @Override
            public int compare(final DownloadPriority o1, final DownloadPriority o2) {
                return (o1.getValue() > o2.getValue()) ? -1 : ((o1.getValue() < o2.getValue()) ? 1 : 0);
            }
        };
    }
}
