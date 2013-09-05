package com.zinc.repo;

import com.google.gson.Gson;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import com.zinc.utils.ZincBaseTest;
import com.zinc.classes.ZincCatalog;
import com.zinc.classes.ZincRepo;
import com.zinc.classes.ZincRepoIndex;
import com.zinc.classes.ZincRepoIndexWriter;
import com.zinc.classes.jobs.AbstractZincJob;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URL;
import java.util.concurrent.ExecutorService;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * User: NachoSoto
 * Date: 9/3/13
 */
public abstract class RepoBaseTest extends ZincBaseTest {
    protected ZincRepo mRepo;

    @Mock
    protected ZincRepo.ZincJobFactory mJobFactory;

    protected ZincRepoIndexWriter mIndexWriter;

    @Mock
    private AbstractZincJob<ZincCatalog> catalogDownloadJob;

    private ExecutorService mExecutor;
    private Gson mGson;

    @Rule
    public final TemporaryFolder rootFolder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
        mExecutor = createExecutorService();
        mGson = createGson();

        mIndexWriter = newRepoIndexWriter();
        mRepo = new ZincRepo(mExecutor, mJobFactory, rootFolder.getRoot().toURI(), mIndexWriter);

        when(mJobFactory.downloadCatalog((URL)anyObject(), anyString())).thenReturn(catalogDownloadJob);
    }

    protected ZincRepoIndexWriter newRepoIndexWriter() {
        return new ZincRepoIndexWriter(rootFolder.getRoot(), mGson);
    }

    protected final ZincRepoIndex readRepoIndex() throws FileNotFoundException {
        final FileReader fileReader = new FileReader(getIndexFile());
        return mGson.fromJson(fileReader, ZincRepoIndex.class);
    }

    protected final File getIndexFile() {
        return new File(rootFolder.getRoot(), "repo.json");
    }
}
