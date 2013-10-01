package com.mindsnacks.zinc.repo;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.mindsnacks.zinc.classes.downloads.PriorityJobQueue;
import com.mindsnacks.zinc.classes.ZincRepo;
import com.mindsnacks.zinc.classes.ZincRepoIndexWriter;
import com.mindsnacks.zinc.classes.data.ZincBundle;
import com.mindsnacks.zinc.classes.data.ZincCloneBundleRequest;
import com.mindsnacks.zinc.classes.data.ZincRepoIndex;
import com.mindsnacks.zinc.exceptions.ZincRuntimeException;
import com.mindsnacks.zinc.utils.TestUtils;
import com.mindsnacks.zinc.utils.ZincBaseTest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * User: NachoSoto
 * Date: 9/3/13
 */
public abstract class ZincRepoBaseTest extends ZincBaseTest {
    protected ZincRepo mRepo;
    protected ZincRepoIndexWriter mIndexWriter;
    protected final String mFlavorName = "retina";

    @Mock protected PriorityJobQueue<ZincCloneBundleRequest, ZincBundle> mQueue;

    protected Gson mGson;
    @Rule public final TemporaryFolder rootFolder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
        mGson = createGson();

        initializeRepo();
    }

    protected void initializeRepo() {
        mIndexWriter = newRepoIndexWriter();
        mRepo = new ZincRepo(mQueue, rootFolder.getRoot().toURI(), mIndexWriter, mFlavorName);
    }

    protected ZincRepoIndexWriter newRepoIndexWriter() {
        return new ZincRepoIndexWriter(rootFolder.getRoot(), mGson);
    }

    protected final ZincRepoIndex readRepoIndex() throws IOException {
        final File indexFile = getIndexFile();

        try {
            return mGson.fromJson(new FileReader(indexFile), ZincRepoIndex.class);
        } catch (JsonSyntaxException e) {
            throw new ZincRuntimeException("Invalid JSON: " + TestUtils.readFile(indexFile), e);
        }
    }

    protected final File getIndexFile() {
        return new File(rootFolder.getRoot(), "repo.json");
    }
}
