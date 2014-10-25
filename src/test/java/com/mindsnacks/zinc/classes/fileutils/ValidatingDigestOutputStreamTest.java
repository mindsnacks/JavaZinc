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
import static org.junit.Assert.assertFalse;

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
    public void testValidStream() throws IOException {
        try{
            mStream.validate(mHash);
        } catch (ValidatingDigestOutputStream.HashFailedException e) {
            assertFalse("Hash validation failed", true);
        }
    }

    @Test
    public void testInvalidStream() throws IOException {
        try{
            mStream.validate(mHash + "NOT_THE_HASH");
            assertFalse("Hash validation failed", true);
        } catch (ValidatingDigestOutputStream.HashFailedException e) {
            // Expected Hash validation fail
        }
    }

    private ValidatingDigestOutputStream setupDigestStream() throws IOException {
        ValidatingDigestOutputStream stream = new ValidatingDigestOutputStream(new ByteArrayOutputStream(), mHashUtil.newDigest());
        OutputStreamWriter writer = new OutputStreamWriter(stream);
        writer.write(mContents);
        writer.flush();

        return stream;
    }
}