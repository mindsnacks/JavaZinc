package com.zinc.classes;

import java.util.ArrayList;
import java.util.List;

/**
 * User: NachoSoto
 * Date: 9/12/13
 */
public class ZincLogging {
    private static List<MessageListener> mListeners = new ArrayList<MessageListener>();

    public static void log(final String message) {
        for (final MessageListener listener : mListeners) {
            listener.logMessage(message);
        }
    }

    public static void addListener(final MessageListener listener) {
        mListeners.add(listener);
    }

    public static void removeListener(final MessageListener listener) {
        mListeners.remove(listener);
    }

    public static interface MessageListener {
        void logMessage(String message);
    }
}
