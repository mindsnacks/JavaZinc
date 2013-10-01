package com.mindsnacks.zinc.classes.jobs;

import java.io.InputStream;
import java.net.URL;

public interface ZincRequestExecutor {
    InputStream get(URL url) throws AbstractZincDownloadJob.DownloadFileError;
}