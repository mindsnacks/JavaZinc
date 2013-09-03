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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Executor;

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
    public void addingSourceURLAddsItToIndexFile() throws MalformedURLException, FileNotFoundException {
        final URL sourceURL = new URL("http://www.google.com");

        // run
        mRepo.addSourceURL(sourceURL);

        // check

        final FileReader fileReader = new FileReader(new File(rootFolder.getRoot(), "repo.json"));
        final RepoIndex index = mGson.fromJson(fileReader, RepoIndex.class);

        assertTrue(index.getSources().contains(sourceURL));
    }
}
