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
public class GzipHelperTest extends ZincBaseTest {
    private GzipHelper mHelper;
    private ZincBundle mBundle;

    @Rule public final TemporaryFolder rootFolder = new TemporaryFolder();

    private final String mFilename = "file.gz";
    private final String mDestination = "file.txt";
    private final String mContents = randomString();

    private File mDestinationFile;

    @Before
    public void setUp() throws Exception {
        mHelper = new GzipHelper();
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
    public void unzipFileRemovesOriginalFile() throws Exception {
        mHelper.unzipFile(mBundle, mFilename, mDestination);

        assertFalse(new File(mBundle, mFilename).exists());
    }

    @Test
    public void moveFile() throws Exception {
        assertFalse(mDestinationFile.exists());

        mHelper.moveFile(mBundle, mFilename, mDestination);

        assertTrue(mDestinationFile.exists());
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
