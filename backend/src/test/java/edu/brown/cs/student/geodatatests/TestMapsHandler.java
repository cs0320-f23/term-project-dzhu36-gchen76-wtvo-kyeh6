package edu.brown.cs.student.geodatatests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.broadbandtests.MockBroadbandQuery;
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
 * Tests the functionality of the MapsHandler class without using mocked data.
 */
public class TestMapsHandler {

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
    Query<String, String> geoSource = new GeoDataSource("data/geodata/fullDownload.json");
    Spark.get("/geodata", new MapsHandler(geoSource));
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
   * Tests that the geodata endpoint can view unfiltered GeoJson data.
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
    assertTrue(responseMap.get("geo_data").toString()
        .contains("{\"type\":\"FeatureCollection\",\"features\":"));
    loadConnection.disconnect();
  }

  /**
   * Tests that the geodata endpoint can depict filtered GeoJson data from inputted maximum and
   * minimum latitude and longitude parameters.
   *
   * @throws IOException if an error occurs with the URI connection.
   */
  @Test
  public void testMapsBoundingBox() throws IOException {
    HttpURLConnection loadConnection = tryRequest("geodata");
    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> responseMap =
        this.adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    String allData = responseMap.get("geo_data").toString();

    loadConnection =
        tryRequest("geodata?min_lat=32&max_lat=37&min_long=-87&max_long=-69");
    assertEquals(200, loadConnection.getResponseCode());
    responseMap =
        this.adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertEquals(2, responseMap.size());
    assertEquals(Set.of("result", "geo_data"), responseMap.keySet());
    assertEquals("success", responseMap.get("result"));
    assertTrue(responseMap.get("geo_data").toString()
        .contains("{\"type\":\"FeatureCollection\",\"features\":"));
    assertTrue(responseMap.get("geo_data").toString().length() < allData.length());
    loadConnection.disconnect();
  }

  /**
   * Tests that the geodata endpoint can depict filtered GeoJson data filtered on an inputted
   * keyword parameter.
   *
   * @throws IOException if an exception thrown in the process of retrieving data.
   */
  @Test
  public void testMapsKeyword() throws IOException {
    HttpURLConnection loadConnection = tryRequest("geodata");
    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> responseMap =
        this.adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    String allData = responseMap.get("geo_data").toString();

    // filter on valid keyword
    loadConnection = tryRequest("geodata?keyword=school");
    assertEquals(200, loadConnection.getResponseCode());
    responseMap = this.adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertEquals(3, responseMap.size());
    assertEquals(Set.of("result", "geo_data", "keyword"), responseMap.keySet());
    assertEquals("success", responseMap.get("result"));
    assertEquals("school", responseMap.get("keyword"));
    assertTrue(responseMap.get("geo_data").toString()
        .contains("{\"type\":\"FeatureCollection\",\"features\":"));
    assertTrue(responseMap.get("geo_data").toString().length() < allData.length());

    // filter on an empty string should return the entire dataset
    loadConnection = tryRequest("geodata?keyword=");
    assertEquals(200, loadConnection.getResponseCode());
    responseMap = this.adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertEquals(3, responseMap.size());
    assertEquals(Set.of("result", "geo_data", "keyword"), responseMap.keySet());
    assertEquals("success", responseMap.get("result"));
    assertEquals("", responseMap.get("keyword"));
    assertTrue(responseMap.get("geo_data").toString()
        .contains("{\"type\":\"FeatureCollection\",\"features\":"));
    assertEquals(responseMap.get("geo_data").toString().length(), allData.length());

    // filtering on an invalid description returns an entire dataset of features
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
   * Uses fuzz testing to test random inputs; also includes some property based testing for bounding
   * box representation inputs.
   *
   * @throws IOException if an exception thrown in the process of retrieving data.
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
      // this is a test for state checking, which we do in the Maps handler, where we check that
      // the min is not greater than the max
      if (minLat > maxLat || minLong > maxLong || minLat < -90 || maxLat > 90 || minLong < -180
          || maxLong > 180) {
        assertEquals("error_bad_request", responseMap.get("result"));
      } else {
        assertEquals("success", responseMap.get("result"));
      }
    }
  }
}
