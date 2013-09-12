package com.zinc.classes.fileutils;

import com.zinc.classes.data.BundleID;
import com.zinc.classes.data.ZincBundle;
import com.zinc.utils.TestUtils;
import com.zinc.utils.ZincBaseTest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.*;
import java.util.zip.GZIPOutputStream;

import static com.zinc.utils.MockFactory.randomString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * User: NachoSoto
 * Date: 9/10/13
 */
public class FileHelperTest extends ZincBaseTest {
    private FileHelper mHelper;
    private ZincBundle mBundle;

    @Rule public final TemporaryFolder rootFolder = new TemporaryFolder();

    private final String mFilename = "file.gz";
    private final String mDestination = "file.txt";
    private final String mContents = randomString();

    private File mDestinationFile;

    @Before
    public void setUp() throws Exception {
        mHelper = new FileHelper();
        mBundle = new ZincBundle(rootFolder.getRoot(), new BundleID("com.mindsnacks.catalog", "mBundle name"), 2);
        mDestinationFile = new File(mBundle, mDestination);

        createGzipFile(mContents, new File(mBundle, mFilename));
    }

    @Test
    public void unzipFileCreatesFile() throws Exception {
        mHelper.unzipFile(mBundle, mFilename, mDestination);

        assertTrue(mDestinationFile.exists());
    }

    @Test
    public void testUnzipFile() throws Exception {
        mHelper.unzipFile(mBundle, mFilename, mDestination);

        assertEquals(mContents, TestUtils.readFile(mDestinationFile));
    }

    @Test
    public void unzipFileDoesntRemoveOriginalFile() throws Exception {
        mHelper.unzipFile(mBundle, mFilename, mDestination);

        assertTrue(new File(mBundle, mFilename).exists());
    }

    @Test
    public void removeFile() throws Exception {
        final File file = new File(mBundle, mFilename);

        assertTrue(file.exists());
        mHelper.removeFile(mBundle, mFilename);
        assertFalse(file.exists());
    }

    @Test
    public void moveFile() throws Exception {
        assertFalse(mDestinationFile.exists());

        mHelper.moveFile(mBundle, mFilename, mDestination);

        assertTrue(mDestinationFile.exists());
    }

    @Test
    public void copyFile() throws Exception {
        final File originFile = new File(mBundle, mFilename);

        assertFalse(mDestinationFile.exists());

        mHelper.copyFile(mBundle, mFilename, mDestination);

        assertTrue(mDestinationFile.exists());
        assertTrue(originFile.exists());
        assertEquals(TestUtils.readFile(originFile), TestUtils.readFile(mDestinationFile));
    }

    private void createGzipFile(final String contents, final File file) throws IOException {
       final FileOutputStream output = new FileOutputStream(file);
        try {
            final Writer writer = new OutputStreamWriter(new GZIPOutputStream(output));
            try {
                writer.write(contents);
            } finally {
                writer.close();
            }
        } finally {
            output.close();
        }
    }
}
