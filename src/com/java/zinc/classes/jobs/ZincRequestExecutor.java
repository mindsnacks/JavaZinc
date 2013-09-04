package zinc.classes.jobs;

import java.io.InputStreamReader;
import java.net.URL;

public interface ZincRequestExecutor {
    InputStreamReader get(URL url);
}