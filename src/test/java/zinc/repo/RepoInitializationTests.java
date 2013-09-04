package zinc.repo;

import org.junit.Assert;
import org.junit.Test;
import zinc.classes.ZincRepoIndex;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * User: NachoSoto
 * Date: 9/3/13
 */
public class RepoInitializationTests extends RepoBaseTest {
    @Test
    public void addingSourceURLCreatesIndexFile() throws MalformedURLException, FileNotFoundException {
        final URL catalogURL = new URL("http://www.google.com");
        final String catalogID = "catalog";

        // run
        mRepo.addSourceURL(catalogURL, catalogID);

        // check
        assertTrue(readRepoIndex().getSources().contains(new URL(catalogURL, catalogID)));
    }

    @Test
    public void addingSourceURLAddsItToIndexFile() throws IOException {
        final URL originalSourceURL = new URL("https://www.google.com/test");

        final URL newCatalogURL = new URL("https://www.mindsnacks.com/");
        final String newCatalogID = "catalog";
        final URL newSourceURL = new URL(newCatalogURL, newCatalogID);

        final FileWriter fileWriter = new FileWriter(getIndexFile());

        fileWriter.write(String.format("{\"sources\": [\"%s\"]}", originalSourceURL));
        fileWriter.close();

        // run
        mRepo.addSourceURL(newCatalogURL, newCatalogID);

        // check
        assertEquals(new HashSet<URL>(Arrays.asList(originalSourceURL, newSourceURL)), readRepoIndex().getSources());
    }

    @Test
    public void addingTrackingRequestAddsItToIndexFile() throws FileNotFoundException {
        // run
        final String bundleID = "com.mindsnacks.games.swell",
                     distribution = "master";

        mRepo.startTrackingBundle(bundleID, distribution);

        // verify
        final ZincRepoIndex.TrackingInfo trackingInfo = readRepoIndex().getTrackingInfo(bundleID);
        Assert.assertEquals(distribution, trackingInfo.getDistribution());
    }
}
