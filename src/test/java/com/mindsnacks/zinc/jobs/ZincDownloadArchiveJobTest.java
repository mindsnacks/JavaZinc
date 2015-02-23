package com.mindsnacks.zinc.jobs;

import com.ice.tar.TarEntry;
import com.ice.tar.TarOutputStream;
import com.mindsnacks.zinc.classes.jobs.ZincDownloadArchiveJob;
import com.mindsnacks.zinc.classes.jobs.ZincRequestExecutor;
import com.mindsnacks.zinc.utils.TestFactory;
import com.mindsnacks.zinc.utils.TestUtils;
import com.mindsnacks.zinc.utils.ZincBaseTest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;

import java.io.*;
import java.net.URL;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * User: NachoSoto
 * Date: 9/4/13
 */
public class ZincDownloadArchiveJobTest extends ZincBaseTest {
    @Mock private ZincRequestExecutor mRequestExecutor;

    @Rule public final TemporaryFolder rootFolder = new TemporaryFolder();

    private final String mFolder = "results";
    private final URL mUrl = TestFactory.createURL("http://mindsnacks.com");

    private ZincDownloadArchiveJob mJob;

    @Before
    public void setUp() throws Exception {
        initializeJob(true);
    }

    @Test
    public void unarchive() throws Exception {
        final String fileContents1 = "this is a sample file",
                     fileContents2 = "some more content";

        final String fileName1 = "file1.txt",
                     fileName2 = "file2.txt";

        final File[] filesToTar = new File[2];
        filesToTar[0] = TestUtils.createFile(rootFolder, fileName1, fileContents1);
        filesToTar[1] = TestUtils.createFile(rootFolder, fileName2, fileContents2);

        final File tar = createTar(filesToTar);

        final InputStream inputStream = new FileInputStream(tar);
        when(mRequestExecutor.get(mUrl)).thenReturn(inputStream);

        run();

        final File resultsFolder = new File(rootFolder.getRoot(), mFolder);

        assertTrue(resultsFolder.exists());
        assertEquals(fileContents1, TestUtils.readFile(new File(resultsFolder, fileName1).getPath()));
        assertEquals(fileContents2, TestUtils.readFile(new File(resultsFolder, fileName2).getPath()));
    }

    @Test
    public void doesntOverrideFile() throws Exception {
        initializeJob(false);

        // create folder
        new File(rootFolder.getRoot(), mFolder).mkdirs();

        run();

        verify(mRequestExecutor, never()).get(any(URL.class));
    }

    private void initializeJob(final boolean override) {
        mJob = new ZincDownloadArchiveJob(mRequestExecutor, mUrl, rootFolder.getRoot(), mFolder, override);
    }

    private File run() throws Exception {
        return mJob.call();
    }

    private File createTar(File[] filesToTar) throws IOException {
        final File tarFile = rootFolder.newFile("result.tar");

        final FileOutputStream dest = new FileOutputStream(tarFile);

        final TarOutputStream tar = new TarOutputStream(new BufferedOutputStream(dest));

        for (final File file : filesToTar){
            final TarEntry tarEntry = new TarEntry(file);
            tarEntry.setName(file.getName());

            tar.putNextEntry(tarEntry);

            final BufferedInputStream origin = new BufferedInputStream(new FileInputStream(file));

            int count;
            byte data[] = new byte[2048];
            while ((count = origin.read(data)) >= 0) {
                tar.write(data, 0, count);
            }

            tar.closeEntry();
            origin.close();
        }

        tar.close();

        return tarFile;
    }
}
