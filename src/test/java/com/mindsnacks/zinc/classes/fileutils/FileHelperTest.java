package com.mindsnacks.zinc.classes.fileutils;

import com.google.common.io.CharStreams;
import com.mindsnacks.zinc.classes.data.BundleID;
import com.mindsnacks.zinc.classes.data.ZincBundle;
import com.mindsnacks.zinc.utils.TestFactory;
import com.mindsnacks.zinc.utils.TestUtils;
import com.mindsnacks.zinc.utils.ZincBaseTest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.*;
import java.util.zip.GZIPOutputStream;

import static com.mindsnacks.zinc.utils.TestFactory.randomString;
import static org.junit.Assert.*;

/**
 * User: NachoSoto
 * Date: 9/10/13
 */
public class FileHelperTest extends ZincBaseTest {
    private FileHelper mHelper;
    private ZincBundle mBundle;

    @Rule public final TemporaryFolder rootFolder = new TemporaryFolder();

    private final String mFilename = "file.gz";
    private final String mDestinationFilename = "file.txt";
    private final String mContents = randomString();

    private File mOriginalFile;
    private File mDestinationFile;

    @Before
    public void setUp() throws Exception {
        mHelper = new FileHelper();
        mBundle = new ZincBundle(rootFolder.getRoot(), new BundleID("com.mindsnacks.catalog", "mBundle name"), 2);
        mOriginalFile = new File(mBundle, mFilename);
        mDestinationFile = new File(mBundle, mDestinationFilename);

        createGzipFile(mContents, new File(mBundle, mFilename));
    }

    @Test
    public void unzipFileCreatesFile() throws Exception {
        mHelper.unzipFile(mBundle, mFilename, mBundle, mDestinationFilename);

        assertTrue(mDestinationFile.exists());
    }

    @Test
    public void testUnzipFile() throws Exception {
        mHelper.unzipFile(mBundle, mFilename, mBundle, mDestinationFilename);

        assertEquals(mContents, TestUtils.readFile(mDestinationFile));
    }

    @Test
    public void unzipFileDoesntRemoveOriginalFile() throws Exception {
        mHelper.unzipFile(mBundle, mFilename, mBundle, mDestinationFilename);

        assertTrue(mOriginalFile.exists());
    }

    @Test
    public void removeFile() throws Exception {
        assertTrue(mOriginalFile.exists());
        mHelper.removeFile(mOriginalFile);
        assertFalse(mOriginalFile.exists());
    }

    @Test
    public void moveFile() throws Exception {
        assertFalse(mDestinationFile.exists());

        mHelper.moveFile(mBundle, mFilename, mBundle, mDestinationFilename);

        assertTrue(mDestinationFile.exists());
    }

    @Test
    public void copyFile() throws Exception {
        assertFalse(mDestinationFile.exists());

        mHelper.copyFile(mBundle, mFilename, mBundle, mDestinationFilename);

        assertTrue(mDestinationFile.exists());
        assertTrue(mOriginalFile.exists());
        assertEquals(TestUtils.readFile(mOriginalFile), TestUtils.readFile(mDestinationFile));
    }

    @Test
    public void readerForFile() throws Exception {
        // prepare
        final String contents = TestFactory.randomString();
        TestFactory.writeToFile(mOriginalFile, contents);

        // run
        final Reader reader = mHelper.readerForFile(mOriginalFile);

        // verify
        assertEquals(contents, CharStreams.toString(reader));
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
