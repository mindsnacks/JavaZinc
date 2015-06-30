package com.mindsnacks.zinc.classes.fileutils;

import com.google.common.io.CharStreams;
import com.google.gson.Gson;
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
    private HashUtil mHashUtil;
    private Gson mGson;
    private FileHelper mHelper;
    private ZincBundle mBundle;

    @Rule public final TemporaryFolder rootFolder = new TemporaryFolder();

    private final String mFilename = "file.gz";
    private final String mDestinationFilename = "file.txt";
    private final String mContents = randomString();

    private File mOriginalFile;
    private File mDestinationFile;
    private String mOriginalHash;
    private String mGzippedHash;

    private final String filename1 = "file1.txt",
                         filename2 = "file2.txt";

    private File file1, file2;

    @Before
    public void setUp() throws Exception {
        mGson = createGson();
        mHashUtil = new HashUtil();
        mHelper = new FileHelper(mGson, mHashUtil);
        mBundle = new ZincBundle(rootFolder.getRoot(), new BundleID("com.mindsnacks.catalog", "mBundle name"), 2);
        mOriginalFile = new File(mBundle, mFilename);
        mDestinationFile = new File(mBundle, mDestinationFilename);

        file1 = TestUtils.createFile(rootFolder, filename1, "file1");
        file2 = TestUtils.createFile(rootFolder, filename2, "file2");

        createGzipFile(mContents, mOriginalFile);
        mOriginalHash = TestUtils.sha1HashString(mContents);
        mGzippedHash = TestUtils.sha1HashString(new FileInputStream(mOriginalFile));
    }

    @Test
    public void testStreamToFile() throws IOException, ValidatingDigestOutputStream.HashFailedException {
        mHelper.streamToFile(new ByteArrayInputStream(mContents.getBytes()), mDestinationFile, mOriginalHash);

        assertEquals(mContents, TestUtils.readFile(mDestinationFile));
    }

    @Test
    public void unzipFileCreatesFile() throws Exception {
        mHelper.unzipFile(mBundle, mFilename, mBundle, mDestinationFilename, mOriginalHash);

        assertTrue(mDestinationFile.exists());
    }

    @Test
    public void testUnzipFile() throws Exception {
        mHelper.unzipFile(mBundle, mFilename, mBundle, mDestinationFilename, mOriginalHash);

        assertEquals(mContents, TestUtils.readFile(mDestinationFile));
    }

    @Test(expected = ValidatingDigestOutputStream.HashFailedException.class)
    public void testFailingUnzipFile() throws Exception {
        mHelper.unzipFile(mBundle, mFilename, mBundle, mDestinationFilename, mOriginalHash + "NOT_THE_HASH");
    }

    @Test
    public void unzipFileDoesntRemoveOriginalFile() throws Exception {
        mHelper.unzipFile(mBundle, mFilename, mBundle, mDestinationFilename, mOriginalHash);

        assertTrue(mOriginalFile.exists());
    }

    @Test
    public void removeFile() throws Exception {
        assertTrue(mOriginalFile.exists());
        mHelper.removeFile(mOriginalFile);
        assertFalse(mOriginalFile.exists());
    }

    @Test
    public void emptyDirectory() throws Exception {
        assertTrue(file1.exists());
        assertTrue(file2.exists());

        // run
        assertTrue(mHelper.emptyDirectory(mBundle));

        assertTrue(mBundle.exists());
        assertFalse(file1.exists());
        assertFalse(file2.exists());
    }

    @Test
    public void removeDirectory() throws Exception {
        final File subFolder = rootFolder.newFolder("subFolder");
        final File file3 = new File(subFolder, "file3.txt");
        TestUtils.writeToFile(file3, "file3");

        assertTrue(file1.exists());
        assertTrue(file2.exists());
        assertTrue(file3.exists());

        // run
        assertTrue(mHelper.removeDirectory(mBundle));

        assertFalse(mBundle.exists());
        assertFalse(file1.exists());
        assertFalse(file2.exists());
        assertFalse(file3.exists());
    }

    @Test
    public void emptyDirectoryWithNonExistentDirectory() throws Exception {
        // run
        assertTrue(mHelper.emptyDirectory(new File(mBundle, "inexistent folder")));

        assertTrue(mBundle.exists());
    }

    @Test
    public void moveFileWithNames() throws Exception {
        assertFalse(mDestinationFile.exists());

        mHelper.moveFile(mBundle, mFilename, mBundle, mDestinationFilename);

        assertTrue(mDestinationFile.exists());
    }

    @Test
    public void moveFile() throws Exception {
        assertFalse(mDestinationFile.exists());

        mHelper.moveFile(
            new File(mBundle, mFilename),
            new File(mBundle, mDestinationFilename));

        assertTrue(mDestinationFile.exists());
    }

    @Test
    public void copyFile() throws Exception {
        assertFalse(mDestinationFile.exists());

        mHelper.copyFile(mBundle, mFilename, mBundle, mDestinationFilename, mGzippedHash);

        assertTrue(mDestinationFile.exists());
        assertTrue(mOriginalFile.exists());
        assertEquals(TestUtils.readFile(mOriginalFile), TestUtils.readFile(mDestinationFile));
    }

    @Test
    public void readerForFile() throws Exception {
        // prepare
        final String contents = TestFactory.randomString();
        TestUtils.writeToFile(mOriginalFile, contents);

        // run
        final Reader reader = mHelper.readerForFile(mOriginalFile);

        // verify
        assertEquals(contents, CharStreams.toString(reader));
    }

    @Test
    public void readJSON() throws Exception {
        // prepare
        final String stringContents = TestFactory.randomString();
        final String JSON = mGson.toJson(stringContents);
        TestUtils.writeToFile(mOriginalFile, JSON);

        // run
        final String result = mHelper.readJSON(mOriginalFile, String.class);

        // verify
        assertEquals(stringContents, result);
    }

    @Test
    public void writeObject() throws Exception {
        // prepare
        final String object = TestFactory.randomString();

        // run
        mHelper.writeObject(mOriginalFile, object, String.class);

        // verify
        assertEquals(object, mGson.fromJson(TestUtils.readFile(mOriginalFile), String.class));
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
