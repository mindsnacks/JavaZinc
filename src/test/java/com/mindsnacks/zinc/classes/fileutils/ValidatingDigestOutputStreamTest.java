package com.mindsnacks.zinc.classes.fileutils;

import com.mindsnacks.zinc.utils.TestUtils;
import com.mindsnacks.zinc.utils.ZincBaseTest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import static com.mindsnacks.zinc.utils.TestFactory.randomString;

public class ValidatingDigestOutputStreamTest extends ZincBaseTest {

    @Rule public final TemporaryFolder rootFolder = new TemporaryFolder();
    private HashUtil mHashUtil;
    private String mContents;
    private String mHash;
    private ValidatingDigestOutputStream mStream;

    @Before
    public void setup() throws IOException {
        mHashUtil = new HashUtil();
        mContents = randomString();
        mHash = TestUtils.sha1HashString(mContents);
        mStream = setupDigestStream();
    }

    @Test
    public void testValidStream() throws ValidatingDigestOutputStream.HashFailedException {
        mStream.validate(mHash);
    }

    @Test(expected = ValidatingDigestOutputStream.HashFailedException.class)
    public void testInvalidStream() throws ValidatingDigestOutputStream.HashFailedException {
        mStream.validate(mHash + "NOT_THE_HASH");
    }

    private ValidatingDigestOutputStream setupDigestStream() throws IOException {
        ValidatingDigestOutputStream stream = new ValidatingDigestOutputStream(new ByteArrayOutputStream(), mHashUtil.newDigest());
        OutputStreamWriter writer = new OutputStreamWriter(stream);
        writer.write(mContents);
        writer.flush();

        return stream;
    }
}