package zinc.jobs;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * User: NachoSoto
 * Date: 9/4/13
 */
public class TestUtils {
    public static String readFile(String path) throws IOException {
        final byte[] encoded = Files.readAllBytes(Paths.get(path));
        return StandardCharsets.UTF_8.decode(ByteBuffer.wrap(encoded)).toString();
    }
}
