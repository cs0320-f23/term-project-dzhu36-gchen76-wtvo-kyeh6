package edu.brown.cs.student.geodatatests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.brown.cs.student.mapserver.datasources.GeoDataSource;
import edu.brown.cs.student.mapserver.datasources.GeoDataSource.FeatureCollection;
import edu.brown.cs.student.mapserver.datasources.Query;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Set;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.Test;

/**
 * Tests the functionality of the GeoDataSource class.
 */
public class TestGeoDataSource {

  /**
   * Tests that GeoDataSource can return unfiltered GeoJson redlining data.
   */
  @Test
  public void testGeoDataSourceUnfiltered()
      throws IOException, URISyntaxException, ParseException, InterruptedException {
    Query<String, String> geoData = new GeoDataSource("data/geodata/fullDownload.json");
    String unfilteredGeoData = geoData.query("");
    assertTrue(unfilteredGeoData.contains("{\"type\":\"FeatureCollection\",\"features\":"));
  }

  /**
   * Tests that GeoDataSource can return filtered GeoJson redlining data based on maximum and
   * minimum latitude and longitude values.
   *
   * @throws Exception if any Exception is thrown in the process of retrieving filtered GeoJson
   *                   data.
   */
  @Test
  public void testGeoDataSourceFiltered() throws Exception {
    Query<String, String> geoData = new GeoDataSource("data/geodata/fullDownload.json");
    String unfilteredGeoData = geoData.query("");

    // Basic filtering case
    String filteredGeoData = geoData.query("33,36,-90,-80");
    // The first entry in the geo data, whose coordinates fall within the queried parameters
    assertTrue(filteredGeoData.contains(
        "\"properties\":{\"state\":\"AL\",\"city\":\"Birmingham\",\"name\":\"Mountain Brook Estates and Country Club Gardens (outside city limits)\""));
    assertTrue(filteredGeoData.length() < unfilteredGeoData.length());

    // Filtering case that should return an empty feature list
    unfilteredGeoData = geoData.query("0.000001,0.0001,0.00000001,0.0001");
    assertEquals("{\"type\":\"FeatureCollection\",\"features\":[]}", unfilteredGeoData);
  }

  /**
   * Tests that GeoDataSource can return filtered GeoJson redlining data based on a keyword.
   *
   * @throws Exception if any Exception is thrown in the process of retrieving filtered GeoJson
   *                   data.
   */
  @Test
  public void testGeoDataSourceKeyword() throws Exception {
    Query<String, String> geoData = new GeoDataSource("data/geodata/fullDownload.json");
    String unfilteredGeoData = geoData.query("");

    String filteredGeoData = geoData.query("school");
    assertTrue(filteredGeoData.contains(
        "\"1b\":\"Graded school in area and close to community business center, churches and adequate transportation.\""));
    assertTrue(filteredGeoData.length() < unfilteredGeoData.length());
  }

  /**
   * Tests that the prevKeywordData map containing previously queried keywords and their
   * corresponding GeoJson data gets updated with each keyword query.
   *
   * @throws Exception if any Exception is thrown in the process of retrieving filtered GeoJson
   *                   data.
   */
  @Test
  public void testGetPrevKeywordData() throws Exception {
    GeoDataSource geoData = new GeoDataSource("data/geodata/fullDownload.json");

    // keyword 1
    geoData.query("schools");
    Map<String, FeatureCollection> prevKeywordData = geoData.getPrevKeywordData();
    assertEquals(1, prevKeywordData.size());
    assertTrue(prevKeywordData.containsKey("schools"));
    assertTrue(prevKeywordData.get("schools").toString()
        .contains("1b=Good transportation - schools - all utilities"));

    // keyword 2
    geoData.query("new");
    prevKeywordData = geoData.getPrevKeywordData();
    assertEquals(2, prevKeywordData.size());
    assertEquals(Set.of("schools", "new"), prevKeywordData.keySet());
    assertTrue(prevKeywordData.get("new").toString().contains(
        "1=This is the newest and best moderately priced residential section of Phoenix"));

    // keyword 1 again (size of map unchanged)
    geoData.query("schools");
    prevKeywordData = geoData.getPrevKeywordData();
    assertEquals(2, prevKeywordData.size());
    assertTrue(prevKeywordData.containsKey("schools"));
    assertTrue(prevKeywordData.get("schools").toString()
        .contains("1b=Good transportation - schools - all utilities"));
  }
}
