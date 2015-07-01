package com.mindsnacks.zinc.classes.fileutils;

import com.mindsnacks.zinc.utils.TestUtils;
import com.mindsnacks.zinc.utils.ZincBaseTest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import java.io.FileInputStream;
import java.io.File;

import java.io.IOException;
import java.io.InputStreamReader;

import static com.mindsnacks.zinc.utils.TestFactory.randomString;

public class ValidatingDigestInputStreamTest extends ZincBaseTest {

    @Rule public final TemporaryFolder rootFolder = new TemporaryFolder();
    private HashUtil mHashUtil;
    private String mContents;
    private String mHash;
    private ValidatingDigestInputStream mStream;
    private File mFile;
    private static final int BUFFER_SIZE = 8192;

    @Before
    public void setup() throws IOException {
        mHashUtil = new HashUtil();
        mContents = randomString();
        mHash = TestUtils.sha1HashString(mContents);
        mFile = TestUtils.createFile(rootFolder, "fail.txt", mContents);
        mStream = setupDigestStream();
    }

    @Test
    public void testValidStream() throws ValidatingDigestInputStream.HashFailedException {
        mStream.validate(mHash);
    }

    @Test(expected = ValidatingDigestInputStream.HashFailedException.class)
    public void testInvalidStream() throws ValidatingDigestInputStream.HashFailedException {
        mStream.validate(mHash + "NOT_THE_HASH");
    }

    private ValidatingDigestInputStream setupDigestStream() throws IOException {
        ValidatingDigestInputStream stream = new ValidatingDigestInputStream(new FileInputStream(mFile), mHashUtil.newDigest());
        final byte[] bytes = new byte[BUFFER_SIZE];
        for (int read = 0; (read = stream.read(bytes, 0, BUFFER_SIZE)) !=-1;) {}
        return stream;
    }
}