package edu.brown.cs.student.geodatatests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.broadbandtests.MockBroadbandQuery;
import edu.brown.cs.student.mapserver.BroadBandHandler;
import edu.brown.cs.student.mapserver.MapsHandler;
import edu.brown.cs.student.mapserver.datasources.GeoDataSource;
import edu.brown.cs.student.mapserver.datasources.Query;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;
import okio.Buffer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spark.Spark;

/**
 * Tests the MapsHandler class using mocked data in a mocked file.
 */
public class TestMapsHandlerMock {

  private JsonAdapter<Map<String, Object>> adapter;

  /**
   * Establishes the port on which URI operations should be run.
   */
  @BeforeAll
  public static void setupOnce() {
    Spark.port(0);
    Logger.getLogger("").setLevel(Level.WARNING);
  }

  /**
   * Sets up the maps endpoint.
   */
  @BeforeEach
  public void setup() throws IOException {
    Query<String, String> mockedGeoSource = new GeoDataSource("data/geodata/mockGeoData.json");
    Spark.get("/geodata", new MapsHandler(mockedGeoSource));
    Query<String, String> mockedBroadbandSource = new MockBroadbandQuery();
    Spark.get("/broadband", new BroadBandHandler(mockedBroadbandSource));
    Spark.awaitInitialization();
    Moshi moshi = new Moshi.Builder().build();
    Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
    this.adapter = moshi.adapter(mapStringObject);
  }

  /**
   * Stops listening to the maps endpoint.
   */
  @AfterEach
  public void tearDown() {
    Spark.unmap("/geodata");
    Spark.awaitStop();
  }

  /**
   * Helper to start a connection to a specific API endpoint. This method is mostly borrowed from
   * the TestSoupAPIHandlers class in the server gear-up program.
   *
   * @param queryParams - the URI for the API endpoint
   * @return the connection for the given URI, just after connecting
   * @throws IOException if the connection fails for some reason
   */
  private HttpURLConnection tryRequest(String queryParams) throws IOException {
    URL requestURL = new URL("http://localhost:" + Spark.port() + "/" + queryParams);
    HttpURLConnection clientConnection = (HttpURLConnection) requestURL.openConnection();
    clientConnection.connect();
    return clientConnection;
  }

  /**
   * Tests that the full geo data can be viewed.
   *
   * @throws IOException if an error occurs with the URI connection.
   */
  @Test
  public void testMapsView() throws IOException {
    HttpURLConnection loadConnection = tryRequest("geodata");
    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> responseMap =
        this.adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertEquals(2, responseMap.size());
    assertEquals(Set.of("result", "geo_data"), responseMap.keySet());
    assertEquals("success", responseMap.get("result"));
    assertTrue(responseMap.get("geo_data").toString().contains(
        "\"coordinates\":[[[[-86.0,33.0],[-86.0,34.0],[-87.0,34.0],[-87.0,33.0],[-86.7,33.5]]]]"));
    assertTrue(responseMap.get("geo_data").toString()
        .contains("\"coordinates\":[[[[-87.0,35.0],[-87.0,34.0],[-88.0,35.0],[-88.3,34.5]]]]"));
    assertTrue(responseMap.get("geo_data").toString()
        .contains("\"state\":\"TX\",\"city\":\"Beaumont\"")); //geometry is null
    loadConnection.disconnect();
  }

  /**
   * Tests filtering for geo data based on a representation of a bounding box with min and max
   * latitude and longitude values.
   *
   * @throws IOException if an error occurs with the URI connection.
   */
  @Test
  public void testMapsBoundingBox() throws IOException {
    HttpURLConnection loadConnection =
        tryRequest("geodata?min_lat=32&max_lat=37&min_long=-87&max_long=-69");
    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> responseMap =
        this.adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertEquals(2, responseMap.size());
    assertEquals(Set.of("result", "geo_data"), responseMap.keySet());
    assertEquals("success", responseMap.get("result"));
    assertTrue(responseMap.get("geo_data").toString().contains(
        "\"coordinates\":[[[[-86.0,33.0],[-86.0,34.0],[-87.0,34.0],[-87.0,33.0],[-86.7,33.5]]]]"));
    assertFalse(responseMap.get("geo_data").toString()
        .contains("\"coordinates\":[[[[-87.0,35.0],[-87.0,34.0],[-88.0,35.0],[-88.3,34.5]]]]"));
    assertFalse(responseMap.get("geo_data").toString()
        .contains("\"state\":\"TX\",\"city\":\"Beaumont\"")); //geometry is null
    loadConnection.disconnect();
  }

  /**
   * Tests keyword searching functionality of geo data using various types of inputs.
   *
   * @throws IOException if an error occurs with the URI connection.
   */
  @Test
  public void testMapsKeyword() throws IOException {
    HttpURLConnection loadConnection =
        tryRequest("geodata?keyword=Description");
    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> responseMap =
        this.adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertEquals(3, responseMap.size());
    assertEquals(Set.of("result", "geo_data", "keyword"), responseMap.keySet());
    assertEquals("success", responseMap.get("result"));
    assertEquals("Description", responseMap.get("keyword"));
    assertTrue(responseMap.get("geo_data").toString()
        .contains("\"neighborhood_id\":244,\"area_description_data\":{\"5\":\"Description.\"}}"));
    assertTrue(responseMap.get("geo_data").toString().contains(
        "\"neighborhood_id\":193,\"area_description_data\":{\"31\":\"83 Description.\"}}"));
    assertFalse(responseMap.get("geo_data").toString()
        .contains("\"neighborhood_id\":8898,\"area_description_data\":{\"\":\"\"}"));

    // more specific keyword test, also tests that we are searching through descriptions and not the numerical key
    loadConnection = tryRequest("geodata?keyword=83");
    assertEquals(200, loadConnection.getResponseCode());
    responseMap = this.adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertEquals(3, responseMap.size());
    assertEquals(Set.of("result", "geo_data", "keyword"), responseMap.keySet());
    assertEquals("success", responseMap.get("result"));
    assertEquals("83", responseMap.get("keyword"));
    assertFalse(responseMap.get("geo_data").toString()
        .contains("\"neighborhood_id\":244,\"area_description_data\":{\"5\":\"Description.\"}}"));
    assertTrue(responseMap.get("geo_data").toString().contains(
        "\"neighborhood_id\":193,\"area_description_data\":{\"31\":\"83 Description.\"}}"));
    assertFalse(responseMap.get("geo_data").toString()
        .contains("\"neighborhood_id\":8898,\"area_description_data\":{\"\":\"\"}"));

    // searching with keyword empty, should return entire geo data
    loadConnection = tryRequest("geodata?keyword=");
    assertEquals(200, loadConnection.getResponseCode());
    responseMap = this.adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertEquals(3, responseMap.size());
    assertEquals(Set.of("result", "geo_data", "keyword"), responseMap.keySet());
    assertEquals("success", responseMap.get("result"));
    assertEquals("", responseMap.get("keyword"));
    assertTrue(responseMap.get("geo_data").toString()
        .contains("\"neighborhood_id\":244,\"area_description_data\":{\"5\":\"Description.\"}}"));
    assertTrue(responseMap.get("geo_data").toString().contains(
        "\"neighborhood_id\":193,\"area_description_data\":{\"31\":\"83 Description.\"}}"));
    assertTrue(responseMap.get("geo_data").toString()
        .contains("\"neighborhood_id\":8898,\"area_description_data\":{\"\":\"\"}"));

    // searching for non-existent keyword
    loadConnection = tryRequest("geodata?keyword=lalalalala");
    assertEquals(200, loadConnection.getResponseCode());
    responseMap = this.adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertEquals(3, responseMap.size());
    assertEquals(Set.of("result", "geo_data", "keyword"), responseMap.keySet());
    assertEquals("success", responseMap.get("result"));
    assertEquals("lalalalala", responseMap.get("keyword"));
    assertEquals("{\"type\":\"FeatureCollection\",\"features\":[]}", responseMap.get("geo_data"));

    loadConnection.disconnect();
  }

  /**
   * Tests that malformed URL requests return informative error messages.
   *
   * @throws IOException if an error occurs with the URI connection.
   */
  @Test
  public void testMapsBadRequest() throws IOException {
    // Random parameter
    HttpURLConnection loadConnection = tryRequest("geodata?random=random");
    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> responseMap =
        this.adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertEquals(1, responseMap.size());
    assertEquals("error_bad_request", responseMap.get("result"));

    loadConnection = tryRequest("geodata?keyword=83&random=random");
    assertEquals(200, loadConnection.getResponseCode());
    responseMap = this.adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertEquals(1, responseMap.size());
    assertEquals("error_bad_request", responseMap.get("result"));

    // Missing max_long
    loadConnection = tryRequest("geodata?min_lat=0&max_lat=0&min_long=0");
    assertEquals(200, loadConnection.getResponseCode());
    responseMap = this.adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertEquals(1, responseMap.size());
    assertEquals("error_bad_request", responseMap.get("result"));

    loadConnection = tryRequest("geodata?min_lat=0&max_lat=0&min_long=0&keyword=83");
    assertEquals(200, loadConnection.getResponseCode());
    responseMap = this.adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertEquals(1, responseMap.size());
    assertEquals("error_bad_request", responseMap.get("result"));

    // Missing min_long
    loadConnection = tryRequest("geodata?min_lat=0&max_lat=0&max_long=0");
    assertEquals(200, loadConnection.getResponseCode());
    responseMap = this.adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertEquals(1, responseMap.size());
    assertEquals("error_bad_request", responseMap.get("result"));

    loadConnection = tryRequest("geodata?min_lat=0&max_lat=0&max_long=0&keyword=83");
    assertEquals(200, loadConnection.getResponseCode());
    responseMap = this.adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertEquals(1, responseMap.size());
    assertEquals("error_bad_request", responseMap.get("result"));

    // Missing max_lat
    loadConnection = tryRequest("geodata?min_lat=0&max_long=0&min_long=0");
    assertEquals(200, loadConnection.getResponseCode());
    responseMap = this.adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertEquals(1, responseMap.size());
    assertEquals("error_bad_request", responseMap.get("result"));

    loadConnection = tryRequest("geodata?min_lat=0&max_long=0&min_long=0&keyword=83");
    assertEquals(200, loadConnection.getResponseCode());
    responseMap = this.adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertEquals(1, responseMap.size());
    assertEquals("error_bad_request", responseMap.get("result"));

    // Missing min_lat
    loadConnection = tryRequest("geodata?max_long=0&max_lat=0&min_long=0");
    assertEquals(200, loadConnection.getResponseCode());
    responseMap = this.adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertEquals(1, responseMap.size());
    assertEquals("error_bad_request", responseMap.get("result"));

    loadConnection = tryRequest("geodata?max_long=0&max_lat=0&min_long=0&keyword=83");
    assertEquals(200, loadConnection.getResponseCode());
    responseMap = this.adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertEquals(1, responseMap.size());
    assertEquals("error_bad_request", responseMap.get("result"));

    // Extra parameter
    loadConnection = tryRequest("geodata?min_lat=0&max_lat=0&min_long=0&max_long=0&random=r");
    assertEquals(200, loadConnection.getResponseCode());
    responseMap = this.adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertEquals(1, responseMap.size());
    assertEquals("error_bad_request", responseMap.get("result"));

    loadConnection = tryRequest("geodata?min_lat=0&max_lat=0&min_long=0&max_long=0&keyword=83");
    assertEquals(200, loadConnection.getResponseCode());
    responseMap = this.adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertEquals(1, responseMap.size());
    assertEquals("error_bad_request", responseMap.get("result"));

    // Min lat out of bounds
    loadConnection = tryRequest("geodata?min_lat=-1000&max_lat=10&min_long=-10&max_long=10");
    assertEquals(200, loadConnection.getResponseCode());
    responseMap = this.adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertEquals(1, responseMap.size());
    assertEquals("error_bad_request", responseMap.get("result"));

    // Max lat out of bounds
    loadConnection = tryRequest("geodata?min_lat=-10&max_lat=1000&min_long=-10&max_long=10");
    assertEquals(200, loadConnection.getResponseCode());
    responseMap = this.adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertEquals(1, responseMap.size());
    assertEquals("error_bad_request", responseMap.get("result"));

    // Min long out of bounds
    loadConnection = tryRequest("geodata?min_lat=-10&max_lat=10&min_long=-1000&max_long=10");
    assertEquals(200, loadConnection.getResponseCode());
    responseMap = this.adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertEquals(1, responseMap.size());
    assertEquals("error_bad_request", responseMap.get("result"));

    // Max long out of bounds
    loadConnection = tryRequest("geodata?min_lat=-10&max_lat=10&min_long=-10&max_long=1000");
    assertEquals(200, loadConnection.getResponseCode());
    responseMap = this.adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertEquals(1, responseMap.size());
    assertEquals("error_bad_request", responseMap.get("result"));

    // Min parameters greater than max parameters
    loadConnection = tryRequest("geodata?min_lat=10&max_lat=-10&min_long=10&max_long=-10");
    assertEquals(200, loadConnection.getResponseCode());
    responseMap = this.adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertEquals(1, responseMap.size());
    assertEquals("error_bad_request", responseMap.get("result"));

    // Parameter values that can't be converted into a double
    loadConnection = tryRequest("geodata?min_lat=no&max_lat=no&min_long=no&max_long=no");
    assertEquals(200, loadConnection.getResponseCode());
    responseMap = this.adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertEquals(1, responseMap.size());
    assertEquals("error_bad_request", responseMap.get("result"));

    loadConnection.disconnect();
  }

  /**
   * Tests complex maps filtering interactions with and without bounding box representations.
   *
   * @throws IOException if an error occurs with the URI connection.
   */
  @Test
  public void testMapsComplex() throws IOException {
    // View data
    HttpURLConnection loadConnection = tryRequest("geodata");
    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> responseMap =
        this.adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertEquals(2, responseMap.size());
    assertEquals(Set.of("result", "geo_data"), responseMap.keySet());
    assertEquals("success", responseMap.get("result"));
    assertTrue(responseMap.get("geo_data").toString().contains(
        "\"coordinates\":[[[[-86.0,33.0],[-86.0,34.0],[-87.0,34.0],[-87.0,33.0],[-86.7,33.5]]]]"));
    assertTrue(responseMap.get("geo_data").toString()
        .contains("\"coordinates\":[[[[-87.0,35.0],[-87.0,34.0],[-88.0,35.0],[-88.3,34.5]]]]"));
    assertTrue(
        responseMap.get("geo_data").toString().contains("\"state\":\"TX\",\"city\":\"Beaumont\""));

    // Bounding box 1
    loadConnection = tryRequest("geodata?min_lat=32&max_lat=37&min_long=-87&max_long=-69");
    assertEquals(200, loadConnection.getResponseCode());
    responseMap = this.adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertEquals(2, responseMap.size());
    assertEquals(Set.of("result", "geo_data"), responseMap.keySet());
    assertEquals("success", responseMap.get("result"));
    assertTrue(responseMap.get("geo_data").toString().contains(
        "\"coordinates\":[[[[-86.0,33.0],[-86.0,34.0],[-87.0,34.0],[-87.0,33.0],[-86.7,33.5]]]]"));
    assertFalse(responseMap.get("geo_data").toString()
        .contains("\"coordinates\":[[[[-87.0,35.0],[-87.0,34.0],[-88.0,35.0],[-88.3,34.5]]]]"));
    assertFalse(responseMap.get("geo_data").toString()
        .contains("\"state\":\"TX\",\"city\":\"Beaumont\"")); //geometry is null

    // Bounding box 2
    loadConnection = tryRequest("geodata?min_lat=0&max_lat=10&min_long=0&max_long=10");
    assertEquals(200, loadConnection.getResponseCode());
    responseMap = this.adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertEquals(2, responseMap.size());
    assertEquals(Set.of("result", "geo_data"), responseMap.keySet());
    assertEquals("success", responseMap.get("result"));
    assertFalse(responseMap.get("geo_data").toString().contains(
        "\"coordinates\":[[[[-86.0,33.0],[-86.0,34.0],[-87.0,34.0],[-87.0,33.0],[-86.7,33.5]]]]"));
    assertFalse(responseMap.get("geo_data").toString()
        .contains("\"coordinates\":[[[[-87.0,35.0],[-87.0,34.0],[-88.0,35.0],[-88.3,34.5]]]]"));
    assertFalse(responseMap.get("geo_data").toString()
        .contains("\"state\":\"TX\",\"city\":\"Beaumont\"")); //geometry is null

    // View data again
    loadConnection = tryRequest("geodata");
    assertEquals(200, loadConnection.getResponseCode());
    responseMap = this.adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertEquals(2, responseMap.size());
    assertEquals(Set.of("result", "geo_data"), responseMap.keySet());
    assertEquals("success", responseMap.get("result"));
    assertTrue(responseMap.get("geo_data").toString().contains(
        "\"coordinates\":[[[[-86.0,33.0],[-86.0,34.0],[-87.0,34.0],[-87.0,33.0],[-86.7,33.5]]]]"));
    assertTrue(responseMap.get("geo_data").toString()
        .contains("\"coordinates\":[[[[-87.0,35.0],[-87.0,34.0],[-88.0,35.0],[-88.3,34.5]]]]"));
    assertTrue(responseMap.get("geo_data").toString()
        .contains("\"state\":\"TX\",\"city\":\"Beaumont\"")); //geometry is null

    // Bounding box 3 (maximum size bounding box can be)
    loadConnection = tryRequest("geodata?min_lat=-90&max_lat=90&min_long=-180&max_long=180");
    assertEquals(200, loadConnection.getResponseCode());
    responseMap = this.adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertEquals(2, responseMap.size());
    assertEquals(Set.of("result", "geo_data"), responseMap.keySet());
    assertEquals("success", responseMap.get("result"));
    assertTrue(responseMap.get("geo_data").toString().contains(
        "\"coordinates\":[[[[-86.0,33.0],[-86.0,34.0],[-87.0,34.0],[-87.0,33.0],[-86.7,33.5]]]]"));
    assertTrue(responseMap.get("geo_data").toString()
        .contains("\"coordinates\":[[[[-87.0,35.0],[-87.0,34.0],[-88.0,35.0],[-88.3,34.5]]]]"));
    assertFalse(responseMap.get("geo_data").toString()
        .contains("\"state\":\"TX\",\"city\":\"Beaumont\"")); //geometry is null

    loadConnection.disconnect();
  }

  /**
   * Tests interactions between the different handlers (MapsHandler and BroadBandHandler).
   *
   * @throws IOException if an error occurs with the URI connection.
   */
  @Test
  public void testMapsInteractions() throws IOException {
    // Geo data
    HttpURLConnection loadConnection = tryRequest("geodata");
    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> responseMap =
        this.adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertEquals(2, responseMap.size());
    assertEquals(Set.of("result", "geo_data"), responseMap.keySet());
    assertEquals("success", responseMap.get("result"));
    assertTrue(responseMap.get("geo_data").toString().contains(
        "\"coordinates\":[[[[-86.0,33.0],[-86.0,34.0],[-87.0,34.0],[-87.0,33.0],[-86.7,33.5]]]]"));
    assertTrue(responseMap.get("geo_data").toString()
        .contains("\"coordinates\":[[[[-87.0,35.0],[-87.0,34.0],[-88.0,35.0],[-88.3,34.5]]]]"));
    assertTrue(
        responseMap.get("geo_data").toString().contains("\"state\":\"TX\",\"city\":\"Beaumont\""));

    // Broadband
    loadConnection = tryRequest("broadband?state=california&county=orange%20county");
    assertEquals(200, loadConnection.getResponseCode());
    responseMap = this.adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertEquals(5, responseMap.size());
    assertEquals(
        Set.of("result", "date & time", "broadband", "county", "state"), responseMap.keySet());
    assertEquals("success", responseMap.get("result"));
    assertEquals("0.0", responseMap.get("broadband"));
    assertEquals("orange county", responseMap.get("county"));
    assertEquals("california", responseMap.get("state"));

    // Geo data with bounding box
    loadConnection = tryRequest("geodata?min_lat=32&max_lat=37&min_long=-87&max_long=-69");
    assertEquals(200, loadConnection.getResponseCode());
    responseMap = this.adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertEquals(2, responseMap.size());
    assertEquals(Set.of("result", "geo_data"), responseMap.keySet());
    assertEquals("success", responseMap.get("result"));
    assertTrue(responseMap.get("geo_data").toString().contains(
        "\"coordinates\":[[[[-86.0,33.0],[-86.0,34.0],[-87.0,34.0],[-87.0,33.0],[-86.7,33.5]]]]"));
    assertFalse(responseMap.get("geo_data").toString()
        .contains("\"coordinates\":[[[[-87.0,35.0],[-87.0,34.0],[-88.0,35.0],[-88.3,34.5]]]]"));
    assertFalse(responseMap.get("geo_data").toString()
        .contains("\"state\":\"TX\",\"city\":\"Beaumont\"")); //geometry is null

    // Keyword 1
    loadConnection = tryRequest("geodata?keyword=Description.");
    assertEquals(200, loadConnection.getResponseCode());
    responseMap = this.adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertEquals(3, responseMap.size());
    assertEquals(Set.of("result", "geo_data", "keyword"), responseMap.keySet());
    assertEquals("success", responseMap.get("result"));
    assertEquals("Description.", responseMap.get("keyword"));
    assertTrue(responseMap.get("geo_data").toString()
        .contains("\"neighborhood_id\":244,\"area_description_data\":{\"5\":\"Description.\"}}"));
    assertTrue(responseMap.get("geo_data").toString().contains(
        "\"neighborhood_id\":193,\"area_description_data\":{\"31\":\"83 Description.\"}}"));
    assertFalse(responseMap.get("geo_data").toString()
        .contains("\"neighborhood_id\":8898,\"area_description_data\":{\"\":\"\"}"));

    // Broadband
    loadConnection = tryRequest("broadband?state=california&county=napa%20county");
    assertEquals(200, loadConnection.getResponseCode());
    responseMap = this.adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertEquals(5, responseMap.size());
    assertEquals(
        Set.of("result", "date & time", "broadband", "county", "state"), responseMap.keySet());
    assertEquals("success", responseMap.get("result"));
    assertEquals("0.0", responseMap.get("broadband"));
    assertEquals("napa county", responseMap.get("county"));
    assertEquals("california", responseMap.get("state"));

    // Keyword 2
    loadConnection = tryRequest("geodata?keyword=83");
    assertEquals(200, loadConnection.getResponseCode());
    responseMap = this.adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertEquals(3, responseMap.size());
    assertEquals(Set.of("result", "geo_data", "keyword"), responseMap.keySet());
    assertEquals("success", responseMap.get("result"));
    assertEquals("83", responseMap.get("keyword"));
    assertFalse(responseMap.get("geo_data").toString()
        .contains("\"neighborhood_id\":244,\"area_description_data\":{\"5\":\"Description.\"}}"));
    assertTrue(responseMap.get("geo_data").toString().contains(
        "\"neighborhood_id\":193,\"area_description_data\":{\"31\":\"83 Description.\"}}"));
    assertFalse(responseMap.get("geo_data").toString()
        .contains("\"neighborhood_id\":8898,\"area_description_data\":{\"\":\"\"}"));

    // Geo data
    loadConnection = tryRequest("geodata");
    assertEquals(200, loadConnection.getResponseCode());
    responseMap = this.adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertEquals(2, responseMap.size());
    assertEquals(Set.of("result", "geo_data"), responseMap.keySet());
    assertEquals("success", responseMap.get("result"));
    assertTrue(responseMap.get("geo_data").toString().contains(
        "\"coordinates\":[[[[-86.0,33.0],[-86.0,34.0],[-87.0,34.0],[-87.0,33.0],[-86.7,33.5]]]]"));
    assertTrue(responseMap.get("geo_data").toString()
        .contains("\"coordinates\":[[[[-87.0,35.0],[-87.0,34.0],[-88.0,35.0],[-88.3,34.5]]]]"));
    assertTrue(
        responseMap.get("geo_data").toString().contains("\"state\":\"TX\",\"city\":\"Beaumont\""));

    loadConnection.disconnect();
  }

  /**
   * Fuzz testing with random inputs; allows for some property-based testing as well.
   *
   * @throws IOException if an error occurs with the URI connection.
   */
  @Test
  public void testRandomBoundingBox() throws IOException {
    for (int i = 0; i < 10000; i++) {
      double minLat = ThreadLocalRandom.current().nextDouble(Double.MIN_VALUE, Double.MAX_VALUE);
      double maxLat = ThreadLocalRandom.current().nextDouble(Double.MIN_VALUE, Double.MAX_VALUE);
      double minLong = ThreadLocalRandom.current().nextDouble(Double.MIN_VALUE, Double.MAX_VALUE);
      double maxLong = ThreadLocalRandom.current().nextDouble(Double.MIN_VALUE, Double.MAX_VALUE);
      HttpURLConnection loadConnection = tryRequest(
          "geodata?min_lat=" + minLat + "&max_lat=" + maxLat + "&min_long=" + minLong + "&max_long="
              + maxLong);
      assertEquals(200, loadConnection.getResponseCode());
      Map<String, Object> responseMap =
          this.adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
      if (minLat > maxLat || minLong > maxLong || minLat < -90 || maxLat > 90 || minLong < -180
          || maxLong > 180) {
        assertEquals("error_bad_request", responseMap.get("result"));
      } else {
        assertEquals("success", responseMap.get("result"));
      }
    }
  }
}
