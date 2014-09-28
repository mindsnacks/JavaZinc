package com.mindsnacks.zinc.classes.fileutils;

import com.mindsnacks.zinc.exceptions.ZincException;
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
import static org.junit.Assert.assertTrue;

public class ValidatingDigestOutputStreamTest extends ZincBaseTest {

    @Rule public final TemporaryFolder rootFolder = new TemporaryFolder();
    private HashUtil mHashUtil;
    private String mContents;
    private String mHash;

    @Before
    public void setup(){
        mHashUtil = new HashUtil();
        mContents = randomString();
        mHash = mHashUtil.sha1HashString(mContents);
    }

    @Test
    public void testValidStream() throws IOException {
        ValidatingDigestOutputStream stream = new ValidatingDigestOutputStream(new ByteArrayOutputStream(), mHashUtil.newDigest());
        OutputStreamWriter writer = new OutputStreamWriter(stream);
        writer.write(mContents);
        writer.flush();

        try{
            stream.validate(mHash);
        } catch (ZincException e) {
            assertFalse("Hash validation failed", true);
        }
    }

    @Test
    public void testInvalidStream() throws IOException {
        ValidatingDigestOutputStream stream = new ValidatingDigestOutputStream(new ByteArrayOutputStream(), mHashUtil.newDigest());
        OutputStreamWriter writer = new OutputStreamWriter(stream);
        writer.write(mContents);
        writer.flush();

        try{
            stream.validate(mHash + "NOT_THE_HASH");
            assertFalse("Hash validation failed", true);
        } catch (ZincException e) {
            assertTrue("Hash validation succeeded", true);
        }
    }

}