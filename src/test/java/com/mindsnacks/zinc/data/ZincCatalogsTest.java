package com.mindsnacks.zinc.data;

import com.google.gson.Gson;
import com.mindsnacks.zinc.classes.ZincJobFactory;
import com.mindsnacks.zinc.classes.data.PathHelper;
import com.mindsnacks.zinc.classes.data.ZincCatalog;
import com.mindsnacks.zinc.classes.data.ZincCatalogs;
import com.mindsnacks.zinc.utils.MockFactory;
import com.mindsnacks.zinc.utils.ZincBaseTest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;

import java.io.Reader;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * User: NachoSoto
 * Date: 10/7/13
 */
public class ZincCatalogsTest extends ZincBaseTest {
    @Rule public final TemporaryFolder rootFolder = new TemporaryFolder();

    @Mock ZincJobFactory mJobFactory;

    private ZincCatalogs catalogs;
    private Gson mGson;

    private final String mCatalogID = "com.mindsnacks.games";

    @Before
    public void setUp() throws Exception {
        mGson = createGson();
        catalogs = new ZincCatalogs(rootFolder.getRoot(), mGson, mJobFactory);
    }

    @Test
    public void returnsLocalCatalogIfExists() throws Exception {
        final ZincCatalog expectedResult = mock(ZincCatalog.class);

        MockFactory.createFile(rootFolder, PathHelper.getLocalCatalogFilePath(mCatalogID), "");
        when(mGson.fromJson(any(Reader.class), ZincCatalog.class)).thenReturn(expectedResult);

        // run
        final Future<ZincCatalog> catalog = catalogs.getCatalog(mCatalogID);

        // verify
        assertNotNull(catalog);
        assertEquals(expectedResult, catalog.get());
    }
}
