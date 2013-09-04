package zinc.repo;

import com.google.gson.Gson;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import utils.BaseTest;
import utils.DirectExecutor;
import zinc.classes.ZincRepo;
import zinc.classes.ZincRepoIndex;
import zinc.classes.jobs.ZincJob;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URL;
import java.util.concurrent.Executor;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * User: NachoSoto
 * Date: 9/3/13
 */
public abstract class RepoBaseTest extends BaseTest {
    protected ZincRepo mRepo;
    protected ZincRepo.ZincJobFactory mJobFactory;

    private Executor mExecutor;

    private Gson mGson;

    @Rule
    public final TemporaryFolder rootFolder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
        mExecutor = new DirectExecutor();
        mGson = createGson();

        mJobFactory = mock(ZincRepo.ZincJobFactory.class);
        mRepo = new ZincRepo(mExecutor, mJobFactory, mGson, rootFolder.getRoot().toURI());

        when(mJobFactory.downloadCatalog((URL)anyObject(), anyString())).thenReturn(mock(ZincJob.class));
    }

    protected ZincRepoIndex readRepoIndex() throws FileNotFoundException {
        final FileReader fileReader = new FileReader(getIndexFile());
        return mGson.fromJson(fileReader, ZincRepoIndex.class);
    }

    protected File getIndexFile() {
        return new File(rootFolder.getRoot(), "repo.json");
    }
}
