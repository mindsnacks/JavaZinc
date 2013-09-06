package com.zinc.repo;

import com.google.gson.Gson;
import com.zinc.classes.*;
import com.zinc.utils.ZincBaseTest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URL;
import java.util.concurrent.Future;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * User: NachoSoto
 * Date: 9/3/13
 */
public abstract class RepoBaseTest extends ZincBaseTest {
    protected ZincRepo mRepo;

    @Mock
    protected ZincFutureFactory mJobFactory;

    protected ZincRepoIndexWriter mIndexWriter;

    @Mock
    private Future<ZincCatalog> futureCatalog;

    private Gson mGson;

    @Rule
    public final TemporaryFolder rootFolder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
        mGson = createGson();

        mIndexWriter = newRepoIndexWriter();
        mRepo = new ZincRepo(mJobFactory, rootFolder.getRoot().toURI(), mIndexWriter);

        when(mJobFactory.downloadCatalog(any(URL.class))).thenReturn(futureCatalog);
    }

    protected ZincRepoIndexWriter newRepoIndexWriter() {
        return new ZincRepoIndexWriter(rootFolder.getRoot(), mGson);
    }

    protected final ZincRepoIndex readRepoIndex() throws FileNotFoundException {
        return mGson.fromJson(new FileReader(getIndexFile()), ZincRepoIndex.class);
    }

    protected final File getIndexFile() {
        return new File(rootFolder.getRoot(), "repo.json");
    }
}
