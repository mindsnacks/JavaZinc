package zinc.repo;

import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * User: NachoSoto
 * Date: 9/3/13
 */
public class RepoInitializationTests extends RepoBaseTest {
    @Test
    public void addingSourceURLCreatesIndexFile() throws MalformedURLException, FileNotFoundException {
        final URL sourceURL = new URL("http://www.google.com");

        // run
        mRepo.addSourceURL(sourceURL);

        // check
        assertTrue(readRepoIndex().getSources().contains(sourceURL));
    }

    @Test
    public void addingSourceURLAddsItToIndexFile() throws IOException {
        final URL sourceURL = new URL("https://www.mindsnacks.com"),
                originalSourceURL = new URL("https://www.google.com");

        final FileWriter fileWriter = new FileWriter(getIndexFile());

        fileWriter.write(String.format("{\"sources\": [\"%s\"]}", originalSourceURL));
        fileWriter.close();

        // run
        mRepo.addSourceURL(sourceURL);

        // check
        assertEquals(new HashSet<URL>(Arrays.asList(originalSourceURL, sourceURL)), readRepoIndex().getSources());
    }
}
