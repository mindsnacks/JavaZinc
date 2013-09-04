package zinc.classes.jobs;

import com.github.kevinsawicki.http.HttpRequest;

import java.io.InputStreamReader;
import java.net.URL;

public interface ZincRequestExecutor {
    InputStreamReader get(URL url) throws HttpRequest.HttpRequestException;
}