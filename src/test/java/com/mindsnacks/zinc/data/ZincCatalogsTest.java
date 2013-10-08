package com.mindsnacks.zinc.data;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.mindsnacks.zinc.classes.ZincJobFactory;
import com.mindsnacks.zinc.classes.data.SourceURL;
import com.mindsnacks.zinc.classes.data.ZincCatalog;
import com.mindsnacks.zinc.classes.data.ZincCatalogs;
import com.mindsnacks.zinc.classes.fileutils.FileHelper;
import com.mindsnacks.zinc.utils.TestFactory;
import com.mindsnacks.zinc.utils.ZincBaseTest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Matchers;
import org.mockito.Mock;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
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

    @Mock ZincJobFactory mJobFactory;
    @Mock FileHelper mFileHelper;
    @Mock ListeningScheduledExecutorService mExecutorService;

    private ZincCatalogs catalogs;

    private final String mCatalogID = "com.mindsnacks.games";
    private final SourceURL mSourceURL;

    public ZincCatalogsTest() throws MalformedURLException {
        mSourceURL = new SourceURL(TestFactory.createURL("http://www.mindsnacks.com"), mCatalogID);
    }

    @Before
    public void setUp() throws Exception {
        catalogs = new ZincCatalogs(rootFolder.getRoot(), mFileHelper, mJobFactory, mExecutorService);
    }

    @Test
    public void returnsLocalCatalogIfExists() throws Exception {
        final ZincCatalog expectedResult = mock(ZincCatalog.class);

        setLocalCatalogFileContent(expectedResult);

        final Future<ZincCatalog> catalog = run();

        // verify
        assertNotNull(catalog);
        assertEquals(expectedResult, catalog.get());
    }

    @Test
    public void catalogIsDownloaded() throws Exception {
        setLocalCatalogFileDoesNotExist();

        run();

        verify(mJobFactory).downloadCatalog(mSourceURL);
    }

    @Test
    public void catalogDownloadIsSubmitted() throws Exception {
        final Callable downloadTask = mock(Callable.class);

        setLocalCatalogFileDoesNotExist();
        doReturn(downloadTask).when(mJobFactory).downloadCatalog(mSourceURL);

        run();

        verify(mExecutorService).submit(downloadTask);
    }

    @Test
    public void returnedFutureComesFromExecutorService() throws Exception {
        final ListenableFuture future = mock(ListenableFuture.class);

        setLocalCatalogFileDoesNotExist();
        doReturn(future).when(mExecutorService).submit(Matchers.<Callable>any());

        assertEquals(future, run());
    }
    
    private void setLocalCatalogFileContent(final ZincCatalog expectedResult) throws FileNotFoundException {
        doReturn(expectedResult).when(mFileHelper).readJSON(any(File.class), eq(ZincCatalog.class));
    }

    private void setLocalCatalogFileDoesNotExist() throws FileNotFoundException {
        doThrow(FileNotFoundException.class).when(mFileHelper).readJSON(any(File.class), any(Class.class));
    }

    private Future<ZincCatalog> run() {
        return catalogs.getCatalog(mSourceURL);
    }
}
