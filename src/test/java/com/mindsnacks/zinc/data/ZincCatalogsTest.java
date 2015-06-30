package com.mindsnacks.zinc.data;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;
import com.mindsnacks.zinc.classes.ZincJobFactory;
import com.mindsnacks.zinc.classes.data.SourceURL;
import com.mindsnacks.zinc.classes.data.ZincCatalog;
import com.mindsnacks.zinc.classes.data.ZincCatalogs;
import com.mindsnacks.zinc.classes.fileutils.FileHelper;
import com.mindsnacks.zinc.exceptions.ZincRuntimeException;
import com.mindsnacks.zinc.utils.TestFactory;
import com.mindsnacks.zinc.utils.ZincBaseTest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.mockito.verification.VerificationMode;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * User: NachoSoto
 * Date: 10/7/13
 */
public class ZincCatalogsTest extends ZincBaseTest {
    @Rule public final TemporaryFolder rootFolder = new TemporaryFolder();

    @Mock private ZincJobFactory mJobFactory;
    @Mock private FileHelper mFileHelper;
    @Spy private Set<SourceURL> mTrackedSourceURLs = new HashSet<SourceURL>();
    @Mock private ListeningExecutorService mExecutorService;
    @Mock private ZincCatalog mResultCatalog;
    @Mock private Timer mTimer;
    @Mock private File mCatalogFile;

    private TimerTask mScheduledTask;
    private boolean runTaskImmediately = false;

    private static final String mCatalogID = "com.mindsnacks.games";
    private final SourceURL mSourceURL;

    private ZincCatalogs catalogs;

    public ZincCatalogsTest() throws MalformedURLException {
        mSourceURL = new SourceURL(TestFactory.createURL("http://www.mindsnacks.com"), mCatalogID);
    }

    @Before
    public void setUp() throws Exception {
        initialize();

        setUpTimerTaskAnswer();
    }

    private void initialize() {
        catalogs = spy(new ZincCatalogs(rootFolder.getRoot(),
                                        mFileHelper,
                                        mTrackedSourceURLs,
                                        mJobFactory,
                                        mExecutorService,
                                        MoreExecutors.sameThreadExecutor(),
                                        mTimer));
        setCatalogFileLength((long)1);
    }

    private void scheduleUpdate() {
        catalogs.scheduleUpdate();
    }


    private void initializeAndScheduleUpdate() {
        initialize();
        scheduleUpdate();
    }


    private Future<ZincCatalog> run() {
        return catalogs.getCatalog(mSourceURL);
    }

    @Test
    public void clearCachedCatalogs() throws Exception {
        catalogs.clearCachedCatalogs();

        verify(mFileHelper).emptyDirectory(any(File.class));
    }

    @Test(expected = ZincRuntimeException.class)
    public void clearCachedCatalogsMustBeCalledBeforeSchedulingUpdates() throws Exception {
        scheduleUpdate();
        catalogs.clearCachedCatalogs();
    }

    @Test
    public void returnsLocalCatalogIfExists() throws Exception {
        setLocalCatalogFileContent();

        final Future<ZincCatalog> catalog = run();

        // verify
        assertNotNull(catalog);
        assertEquals(mResultCatalog, catalog.get());
    }

    @Test
    public void JSONIsReadFromDisk() throws Exception {
        setLocalCatalogFileContent();

        run();

        verify(mFileHelper).readJSON(any(File.class), eq(ZincCatalog.class));
    }

    @Test
    public void catalogIsDownloaded() throws Exception {
        setLocalCatalogFileDoesNotExist();
        setMockFutureAsResult();

        run();

        verifyCatalogIsDownloaded();
    }

    @Test
    public void catalogDownloadIsSubmitted() throws Exception {
        final Callable downloadTask = mock(Callable.class);

        setLocalCatalogFileDoesNotExist();
        setMockFutureAsResult();
        doReturn(downloadTask).when(mJobFactory).downloadCatalog(mSourceURL);

        run();

        verify(mExecutorService).submit(downloadTask);
    }

    @Test
    public void returnedFutureComesFromExecutorService() throws Exception {
        setLocalCatalogFileDoesNotExist();
        final ListenableFuture future = setMockFutureAsResult();

        assertEquals(future, run());
    }

    @Test
    public void catalogIsPersisted() throws Exception {
        final SettableFuture<ZincCatalog> future = SettableFuture.create();
        future.set(mResultCatalog);

        setLocalCatalogFileDoesNotExist();
        setFuture(future);

        run();

        verify(mFileHelper).writeObject(any(File.class), eq(mResultCatalog), eq(ZincCatalog.class));
    }

    @Test
    public void futuresAreCached() throws Exception {
        setLocalCatalogFileContent();

        final Future<ZincCatalog> future1 = run(),
                                  future2 = run();

        assertEquals(future1, future2);

        verify(mFileHelper, times(1)).readJSON(any(File.class), eq(ZincCatalog.class));
    }

    @Test
    public void gettingCatalogForSourceURLAddsItToTheListOfTrackedSources() throws Exception {
        run();

        verify(mTrackedSourceURLs).add(mSourceURL);
    }

    @Test
    public void testScheduleUpdate() throws Exception {
        scheduleUpdate();

        verify(mTimer).schedule(any(TimerTask.class), anyLong(), anyLong());
    }

    @Test
    public void canOnlyScheduleUpdatesOnce() throws Exception {
        scheduleUpdate();
        scheduleUpdate();

        verify(mTimer, times(1)).schedule(any(TimerTask.class), anyLong(), anyLong());
    }

    @Test
    public void updatingCatalogsDownloadsNewOnes() throws Exception {
        setUpUpdateTask();

        initializeAndScheduleUpdate();

        verifyCatalogIsDownloaded();
    }

    @Test
    public void updatingCatalogsIgnoresCachedCatalogs() throws Exception {
        setUpUpdateTask();

        initializeAndScheduleUpdate();

        verify(mFileHelper, times(0)).readJSON(any(File.class), any(Class.class));
    }

    @Test
    public void updatingCatalogsReplacesFutures() throws Exception {
        final ListenableFuture future = setUpUpdateTask();

        initializeAndScheduleUpdate();

        assertEquals(future, catalogs.getCatalog(mSourceURL));
    }

    @Test
    public void failedCatalogDownloadReplacesFutureWithOriginalOne() throws Exception {
        initializeAndScheduleUpdate();

        // cache the future with the persisted copy
        setLocalCatalogFileContent();
        final Future<ZincCatalog> originalFuture = run();

        setDownloadTaskFails();

        // trigger an update
        runScheduledTask();

        // update task failed, so this should return the original future
        assertEquals(originalFuture, catalogs.getCatalog(mSourceURL));
    }

    @Test
    public void failedCatalogDownloadRemovesCachedFuture() throws Exception {
        setUpUpdateTask();
        setDownloadTaskFails();
        setLocalCatalogFileDoesNotExist();

        initializeAndScheduleUpdate();

        final Future<ZincCatalog> catalogFuture = catalogs.getCatalog(mSourceURL);

        assertNotNull(catalogFuture);
        verifyCatalogIsDownloaded(times(2));
    }

    private void setLocalCatalogFileContent() throws FileNotFoundException {
        doReturn(mResultCatalog).when(mFileHelper).readJSON(any(File.class), eq(ZincCatalog.class));
    }

    private void setLocalCatalogFileDoesNotExist() throws FileNotFoundException {
        doThrow(FileNotFoundException.class).when(mFileHelper).readJSON(any(File.class), any(Class.class));
    }

    private void setDownloadTaskFails() {
        final SettableFuture<ZincCatalog> future = SettableFuture.create();
        future.setException(new ZincRuntimeException("Something went wrong downloading catalog"));
        setFuture(future);
    }

    private void verifyCatalogIsDownloaded() {
        verifyCatalogIsDownloaded(atLeastOnce());
    }

    private void verifyCatalogIsDownloaded(final VerificationMode times) {
        verify(mJobFactory, times).downloadCatalog(mSourceURL);
    }

    private ListenableFuture setMockFutureAsResult() {
        final ListenableFuture future = mock(ListenableFuture.class);

        setFuture(future);

        return future;
    }

    private ListenableFuture setUpUpdateTask() {
        makeTimerRunTaskWhenScheduled();
        mTrackedSourceURLs.add(mSourceURL);
        return setMockFutureAsResult();
    }

    private void makeTimerRunTaskWhenScheduled() {
        runTaskImmediately = true;
    }

    private void setFuture(final ListenableFuture future) {
        doReturn(future).when(mExecutorService).submit(Matchers.<Callable>any());
    }

    private void setUpTimerTaskAnswer() {
        doAnswer(new Answer() {
            @Override
            public Object answer(final InvocationOnMock invocationOnMock) throws Throwable {
                mScheduledTask = (TimerTask)invocationOnMock.getArguments()[0];

                if (runTaskImmediately) {
                    runScheduledTask();
                }

                return null;
            }
        }).when(mTimer).schedule(any(TimerTask.class), anyLong(), anyLong());
    }

    private void setCatalogFileLength(final long length) {
        doReturn((long)1).when(mCatalogFile).length();
        doReturn(mCatalogFile).when(catalogs).getCatalogFile(mSourceURL);
    }

    private void runScheduledTask() {
        mScheduledTask.run();
    }
}
