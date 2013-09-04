package zinc.repo;

import org.junit.Test;
import org.mockito.Mock;
import zinc.classes.ZincCatalog;
import zinc.classes.jobs.ZincJob;

import java.net.URL;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static utils.MockFactory.createCatalogWithIdentifier;
import static utils.MockFactory.randomString;

/**
 * User: NachoSoto
 * Date: 9/3/13
 */
public class RepoJobTests extends RepoBaseTest {
    @Mock
    private ZincJob<ZincCatalog> mZincCatalogDownloadJob;

    @Test
    public void catalogGetsDownloadedWhenAddingTheSource() throws Exception {
        final URL catalogURL = new URL("https://mindsnacks.com");
        final String catalogID = randomString();
        final ZincCatalog catalog = createCatalogWithIdentifier(catalogID);

        when(mZincCatalogDownloadJob.call()).thenReturn(catalog);
        when(mJobFactory.downloadCatalog(eq(catalogURL), eq(catalogID))).thenReturn(mZincCatalogDownloadJob);

        // run
        mRepo.addSourceURL(catalogURL, catalogID);

        verify(mJobFactory).downloadCatalog(eq(catalogURL), eq(catalogID));
    }
}
