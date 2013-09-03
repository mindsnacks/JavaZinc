package utils;

import java.util.concurrent.Executor;

/**
 * User: NachoSoto
 * Date: 9/3/13
 */
public class DirectExecutor implements Executor {
    @Override
    public void execute(final Runnable command) {
        command.run();
    }
}
