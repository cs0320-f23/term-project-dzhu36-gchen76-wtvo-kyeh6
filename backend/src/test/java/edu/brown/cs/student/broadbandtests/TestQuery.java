package edu.brown.cs.student.broadbandtests;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.testng.AssertJUnit.assertEquals;

import edu.brown.cs.student.mapserver.datasources.AcsDataSource;
import edu.brown.cs.student.mapserver.datasources.Query;
import java.io.EOFException;
import java.io.IOException;
import java.net.URISyntaxException;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.Test;

/**
 * Tests the functionality of the classes that implement the Query interface, such as ApiCall.
 */
public class TestQuery {

  /**
   * Tests that the correct broadband is returned after making a request to the ACS API with
   * specific state and county identifiers.
   */
  @Test
  public void testQueryBroadbandBasic()
      throws URISyntaxException, IOException, InterruptedException, ParseException {
    Query<String, String> apiData = new AcsDataSource();
    // Orange County, California
    String broadband =
        apiData.query(
            "https://api.census.gov/data/2021/acs/acs1/subject/variables?get=NAME,S2802_C03_022E&for=county:059&in=state:06");
    assertEquals("93.0", broadband);

    // Clarke County, Georgia
    broadband =
        apiData.query(
            "https://api.census.gov/data/2021/acs/acs1/subject/variables?get=NAME,S2802_C03_022E&for=county:059&in=state:13");
    assertEquals("80.9", broadband);
  }

  /**
   * Tests that an Exception is thrown when a state or county exists in the state/county APIs but
   * not in the ACS API.
   */
  @Test
  public void testQueryBroadbandMissingData() throws IOException {
    Query<String, String> apiData = new AcsDataSource();
    // Colusa County, California is in the county codes API but not in the broadband API
    assertThrows(
        EOFException.class,
        () ->
            apiData.query(
                "https://api.census.gov/data/2021/acs/acs1/subject/variables?get=NAME,S2802_C03_022E&for=county:011&in=state:06"));
  }
}
