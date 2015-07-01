package com.mindsnacks.zinc.data;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;
import com.google.gson.JsonSyntaxException;
import com.mindsnacks.zinc.classes.ZincJobFactory;
import com.mindsnacks.zinc.classes.data.SourceURL;
import com.mindsnacks.zinc.classes.data.ZincManifest;
import com.mindsnacks.zinc.classes.data.ZincManifests;
import com.mindsnacks.zinc.classes.fileutils.FileHelper;
import com.mindsnacks.zinc.utils.TestFactory;
import com.mindsnacks.zinc.utils.ZincBaseTest;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.verification.VerificationMode;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.atLeastOnce;

/**
 * Created by Miguel Carranza on 6/30/15.
 */
public class ZincManifestsTest extends ZincBaseTest {
    @Rule public final TemporaryFolder rootFolder = new TemporaryFolder();

    @Mock private ZincJobFactory mJobFactory;
    @Mock private FileHelper mFileHelper;
    @Mock private ListeningExecutorService mExecutorService;
    @Mock private ZincManifest mResultManifest;
    @Mock private File mManifestFile;

    private static final String mCatalogID = "com.mindsnacks.games";
    private static final String mBundleName = "bundle";
    private static final int mVersion = 25;

    private final SourceURL mSourceURL;
    private ZincManifests manifests;

    public ZincManifestsTest() throws MalformedURLException {
        mSourceURL = new SourceURL(TestFactory.createURL("http://www.mindsnacks.com"), mCatalogID);
    }

    @Before
    public void setUp() throws Exception {
        initialize();
    }

    private void initialize() {
        manifests = spy(new ZincManifests(rootFolder.getRoot(),
                                          mFileHelper,
                                          mJobFactory,
                                          mExecutorService,
                                          MoreExecutors.sameThreadExecutor()));
        setLocalManifestFileLength((long) 1);
    }

    private Future<ZincManifest> run() {
        return manifests.getManifest(mSourceURL, mBundleName, mVersion);
    }

    @Test
    public void clearCachedManifests() throws Exception {
        manifests.clearCachedManifests();

        verify(mFileHelper).emptyDirectory(any(File.class));
    }

    @Test
    public void returnsLocalManifestIfExists() throws Exception {
        setLocalManifestFileContent();

        final Future<ZincManifest> manifest = run();

        // verify
        assertNotNull(manifest);
        assertEquals(mResultManifest, manifest.get());
    }

    @Test
    public void JSONIsReadFromDisk() throws Exception {
        setLocalManifestFileContent();

        run();

        verify(mFileHelper).readJSON(any(File.class), eq(ZincManifest.class));
    }

    @Test
    public void manifestIsDownloadedIfFileNotPresent() throws Exception {
        setLocalManifestFileDoesNotExist();
        setMockFutureAsResult();

        run();

        verifyManifestIsDownloaded();
    }

    @Test
    public void manifestIsDownloadedIfFileSizeIsZero() throws Exception {
        setLocalManifestFileContent();
        setLocalManifestFileLength(0);

        setMockFutureAsResult();

        run();

        verifyManifestIsDownloaded();
    }

    @Test
    public void manifestIsDownloadedIfFileContainsInvalidJSON() throws Exception {
        setLocalManifestFileContainsInvalidJSON();

        setMockFutureAsResult();

        run();

        verifyManifestIsDownloaded();
    }

    @Test
    public void manifestDownloadIsSubmitted() throws Exception {
        final Callable downloadTask = mock(Callable.class);

        setLocalManifestFileDoesNotExist();
        setMockFutureAsResult();
        doReturn(downloadTask).when(mJobFactory).downloadManifest(mSourceURL, mBundleName, mVersion);

        run();

        verify(mExecutorService).submit(downloadTask);
    }

    @Test
    public void returnedFutureComesFromExecutorService() throws Exception {
        setLocalManifestFileDoesNotExist();
        final ListenableFuture future = setMockFutureAsResult();

        assertEquals(future, run());
    }

    @Test
    public void manifestIsPersisted() throws Exception {
        final SettableFuture<ZincManifest> future = SettableFuture.create();
        future.set(mResultManifest);

        setLocalManifestFileDoesNotExist();
        setFuture(future);

        run();

        verify(mFileHelper).writeObject(any(File.class), eq(mResultManifest), eq(ZincManifest.class));
    }

    @Test
    public void futuresAreCached() throws Exception {
        setLocalManifestFileContent();

        final Future<ZincManifest> future1 = run(),
                                  future2 = run();

        assertEquals(future1, future2);

        verify(mFileHelper, times(1)).readJSON(any(File.class), eq(ZincManifest.class));
    }

    private void setLocalManifestFileContent() throws FileNotFoundException {
        doReturn(mResultManifest).when(mFileHelper).readJSON(any(File.class), eq(ZincManifest.class));
    }

    private void setLocalManifestFileDoesNotExist() throws FileNotFoundException {
        doThrow(FileNotFoundException.class).when(mFileHelper).readJSON(any(File.class), any(Class.class));
    }

    private void setLocalManifestFileContainsInvalidJSON() throws FileNotFoundException {
        doThrow(JsonSyntaxException.class).when(mFileHelper).readJSON(any(File.class), any(Class.class));
    }

    private void verifyManifestIsDownloaded() {
        verifManifestIsDownloaded(atLeastOnce());
    }

    private void verifManifestIsDownloaded(final VerificationMode times) {
        verify(mJobFactory, times).downloadManifest(mSourceURL, mBundleName, mVersion);
    }

    private ListenableFuture setMockFutureAsResult() {
        final ListenableFuture future = mock(ListenableFuture.class);

        setFuture(future);

        return future;
    }

    private void setFuture(final ListenableFuture future) {
        doReturn(future).when(mExecutorService).submit(Matchers.<Callable>any());
    }

    private void setLocalManifestFileLength(final long length) {
        doReturn((long)length).when(mManifestFile).length();
        doReturn(mManifestFile).when(manifests).getManifestFile(eq(mCatalogID), anyString());
    }
}
