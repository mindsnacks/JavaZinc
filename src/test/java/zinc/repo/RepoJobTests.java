package zinc.repo;

import org.junit.Test;
import utils.MockFactory;
import zinc.classes.ZincCatalog;
import zinc.classes.jobs.ZincJob;

import java.net.URL;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static utils.MockFactory.randomString;

/**
 * User: NachoSoto
 * Date: 9/3/13
 */
public class RepoJobTests extends RepoBaseTest {
    @Test
    public void catalogGetsDownloadedWhenAddingTheSource() throws Exception {
        final URL catalogURL = new URL("https://mindsnacks.com");
        final String catalogID = randomString();
        final ZincCatalog catalog = MockFactory.createCatalogWithIdentifier(catalogID);

        final ZincJob job = mock(ZincJob.class);

        when(job.call()).thenReturn(catalog);
        when(mJobFactory.downloadCatalog(eq(catalogURL), eq(catalogID))).thenReturn(job);

        // run
        mRepo.addSourceURL(catalogURL, catalogID);

        verify(mJobFactory).downloadCatalog(eq(catalogURL), eq(catalogID));
    }
}
