package utils;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * User: NachoSoto
 * Date: 9/3/13
 */
public class DirectExecutorService extends AbstractExecutorService {
    private volatile boolean shutdown = false;

    public void execute(Runnable command) {
        command.run();
    }

    public void shutdown() {
        shutdown = true;
    }

    public List<Runnable> shutdownNow() {
        shutdown();

        return Collections.emptyList();
    }

    public boolean isShutdown() {
        return shutdown;
    }

    public boolean isTerminated() {
        return shutdown;
    }

    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return shutdown;
    }
}
