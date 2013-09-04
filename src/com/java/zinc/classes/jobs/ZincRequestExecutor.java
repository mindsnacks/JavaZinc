package zinc.classes.jobs;

import com.github.kevinsawicki.http.HttpRequest;

import java.io.InputStream;
import java.net.URL;

public interface ZincRequestExecutor {
    InputStream get(URL url) throws HttpRequest.HttpRequestException;
}