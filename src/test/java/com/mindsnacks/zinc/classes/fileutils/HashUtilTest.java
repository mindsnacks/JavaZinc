package com.mindsnacks.zinc.classes.fileutils;

import com.mindsnacks.zinc.exceptions.ZincRuntimeException;
import com.mindsnacks.zinc.utils.ZincBaseTest;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.*;

public class HashUtilTest extends ZincBaseTest {

    private HashUtil mHashUtil;

    @Before
    public void setup() {
        mHashUtil = new HashUtil();
    }

    @Test
    public void testHash() {
        String input = "test\n";
        assertEquals("4e1243bd22c66e76c2ba9eddc1f91394e57f9f83",
                mHashUtil.sha1HashString(new ByteArrayInputStream(input.getBytes())));
    }

    @Test
    public void testIOException() throws IOException {
        InputStream thowingInputStream = mock(InputStream.class);

        when(thowingInputStream.read(any(byte[].class))).thenThrow(new IOException());

        try {
            mHashUtil.sha1HashString(thowingInputStream);
            assertFalse("Expected exception not thrown", true);
        } catch (ZincRuntimeException e) {
            // Expected ZincRuntimeException
        }
    }
}