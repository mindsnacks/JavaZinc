package zinc;

import com.google.gson.Gson;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import utils.BaseTest;
import utils.DirectExecutor;
import zinc.classes.Repo;
import zinc.classes.RepoIndex;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.Executor;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * User: NachoSoto
 * Date: 9/3/13
 */
public class RepoTests extends BaseTest {
    private Executor mExecutor;
    private Repo mRepo;
    private Gson mGson;

    @Rule
    public final TemporaryFolder rootFolder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
        mExecutor = new DirectExecutor();
        mGson = createGson();

        mRepo = new Repo(mExecutor, mGson, rootFolder.getRoot().toURI());
    }

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
                originalSourceURl = new URL("https://www.google.com");

        final FileWriter fileWriter = new FileWriter(getIndexFile());

        fileWriter.write("{\n" +
                "  \"sources\": [\n" +
                "    \"" + originalSourceURl + "\"\n" +
                "  ]\n" +
                "}");
        fileWriter.close();

        // run
        mRepo.addSourceURL(sourceURL);

        // check
        assertEquals(new HashSet<URL>(Arrays.asList(originalSourceURl, sourceURL)), readRepoIndex().getSources());
    }

    private RepoIndex readRepoIndex() throws FileNotFoundException {
        final FileReader fileReader = new FileReader(getIndexFile());
        return mGson.fromJson(fileReader, RepoIndex.class);
    }

    private File getIndexFile() {
        return new File(rootFolder.getRoot(), "repo.json");
    }
}
