package zinc.repo;

import com.google.gson.Gson;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import utils.BaseTest;
import utils.DirectExecutor;
import zinc.classes.Repo;
import zinc.classes.RepoIndex;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.concurrent.Executor;

/**
 * User: NachoSoto
 * Date: 9/3/13
 */
public abstract class RepoBaseTest extends BaseTest {
    protected Repo mRepo;

    private Executor mExecutor;
    private Gson mGson;

    @Rule
    public final TemporaryFolder rootFolder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
        mExecutor = new DirectExecutor();
        mGson = createGson();

        mRepo = new Repo(mExecutor, mGson, rootFolder.getRoot().toURI());
    }

    protected RepoIndex readRepoIndex() throws FileNotFoundException {
        final FileReader fileReader = new FileReader(getIndexFile());
        return mGson.fromJson(fileReader, RepoIndex.class);
    }

    protected File getIndexFile() {
        return new File(rootFolder.getRoot(), "repo.json");
    }
}
