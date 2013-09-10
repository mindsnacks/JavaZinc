package com.zinc.jobs;

import com.zinc.classes.ZincFutureFactory;
import com.zinc.classes.data.*;
import com.zinc.classes.fileutils.GzipHelper;
import com.zinc.classes.jobs.ZincUnarchiveBundleJob;
import com.zinc.utils.MockFactory;
import com.zinc.utils.ZincBaseTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

import static com.zinc.utils.MockFactory.randomInt;
import static com.zinc.utils.MockFactory.randomString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * User: NachoSoto
 * Date: 9/10/13
 */
public class ZincUnarchiveBundleJobTest extends ZincBaseTest {

    private ZincBundleCloneRequest mBundleCloneRequest;

    private ZincUnarchiveBundleJob mJob;

    final private String mBundleName = "swell";
    final private String mCatalogID = "com.mindsnacks.games";
    final private BundleID mBundleID = new BundleID(mCatalogID, mBundleName);
    final private String mDistribution = "master";
    final private String mFlavorName = "retina";
    final private int mVersion = randomInt(5, 100);
    final private URL mSourceHost;

    @Mock private ZincBundle mBundle;
    private Future<ZincBundle> mBundleFuture;
    @Mock private ZincManifest mManifest;
    private Future<ZincManifest> mManifestFuture;
    @Mock private ZincFutureFactory mFutureFactory;
    @Mock private SourceURL mSourceURL;
    @Mock private File mRepoFolder;
    @Mock private GzipHelper mGzipHelper;

    public ZincUnarchiveBundleJobTest() throws MalformedURLException {
        mSourceHost = new URL("https://mindsnacks.com/");
    }

    @Before
    public void setUp() throws Exception {
        mBundleCloneRequest = new ZincBundleCloneRequest(mSourceURL, mBundleID, mDistribution, mFlavorName, mRepoFolder);
        mBundleFuture = MockFactory.createFutureWithResult(mBundle);
        mManifestFuture = MockFactory.createFutureWithResult(mManifest);

        when(mBundle.getBundleID()).thenReturn(mBundleID);
        when(mBundle.getVersion()).thenReturn(mVersion);

        when(mSourceURL.getUrl()).thenReturn(mSourceHost);
        when(mSourceURL.getCatalogID()).thenReturn(mCatalogID);

        when(mFutureFactory.downloadManifest(eq(mSourceURL), eq(mBundleName), eq(mVersion))).thenReturn(mManifestFuture);

        mJob = new ZincUnarchiveBundleJob(mBundleFuture, mBundleCloneRequest, mFutureFactory, mGzipHelper);
    }

    @Test
    public void downloadsTheBundle() throws Exception {
        run();

        verify(mBundleFuture).get();
    }

    @Test
    public void downloadsTheManifest() throws Exception {
        run();

        verify(mFutureFactory).downloadManifest(mSourceURL, mBundleName, mVersion);
    }

    @Test
    public void getsFilesForFlavor() throws Exception {
        run();

        verify(mManifest).getFilesWithFlavor(mFlavorName);
    }

    @Test
    public void unzipsAllFiles() throws Exception {
        final String filename1 = "file1.txt", filename2 = "file2.png";
        final String hash1 = randomString(), hash2 = randomString();
        final Map<String, String> files = new HashMap<String, String>();

        files.put(filename1, hash1);
        files.put(filename2, hash2);

        when(mManifest.getFilesWithFlavor(eq(mFlavorName))).thenReturn(files);

        run();

        verify(mManifest).getFilesWithFlavor(mFlavorName);
        verify(mGzipHelper).unzipFile(eq(mBundle), eq(hash1), eq(filename1));
        verify(mGzipHelper).unzipFile(eq(mBundle), eq(hash2), eq(filename2));
    }


    private ZincBundle run() throws Exception {
        return mJob.call();
    }
}
