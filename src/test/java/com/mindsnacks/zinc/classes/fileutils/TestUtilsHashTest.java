package com.mindsnacks.zinc.classes.fileutils;

import com.mindsnacks.zinc.utils.TestUtils;
import com.mindsnacks.zinc.utils.ZincBaseTest;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class TestUtilsHashTest extends ZincBaseTest {

    @Test
    public void testHash() throws IOException {
        String input = "test\n";
        assertEquals("4e1243bd22c66e76c2ba9eddc1f91394e57f9f83",
                TestUtils.sha1HashString(new ByteArrayInputStream(input.getBytes())));
    }

    @Test(expected = IOException.class)
    public void testIOException() throws IOException {
        InputStream throwingInputStream = mock(InputStream.class);

        when(throwingInputStream.read(any(byte[].class))).thenThrow(new IOException());

        TestUtils.sha1HashString(throwingInputStream);
    }
}