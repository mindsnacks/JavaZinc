package com.mindsnacks.zinc.classes.fileutils;

import com.mindsnacks.zinc.exceptions.ZincRuntimeException;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class HashUtilTest {

    @Test
    public void testHash() {
        String input = "test\n";
        assertEquals("4e1243bd22c66e76c2ba9eddc1f91394e57f9f83",
                HashUtil.sha1HashString(new ByteArrayInputStream(input.getBytes())));
    }

    @Test
    public void testIOException() throws IOException {
        InputStream thowingInputStream = mock(InputStream.class);

        when(thowingInputStream.read(any(byte[].class))).thenThrow(new IOException());

        try {
            HashUtil.sha1HashString(thowingInputStream);
            assertFalse("Expected exception not thrown", true);
        } catch (ZincRuntimeException e) {
            assertTrue(true);
        }
    }
}