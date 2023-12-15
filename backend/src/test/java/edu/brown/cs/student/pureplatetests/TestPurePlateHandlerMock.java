package edu.brown.cs.student.pureplatetests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.pureplate.PurePlateHandler;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import okio.Buffer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spark.Spark;

/**
 * Tests the PurePlateHandler class using mocked data.
 */
public class TestPurePlateHandlerMock {

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
   * Sets up the pureplate endpoint.
   */
  @BeforeEach
  public void setup() throws IOException {
    Spark.get("/pureplate", new PurePlateHandler(new MockNutritionDataSource("data/nutrition/daily_requirements_mocked.csv")));
    Spark.awaitInitialization();
    Moshi moshi = new Moshi.Builder().build();
    Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
    this.adapter = moshi.adapter(mapStringObject);
  }

  /**
   * Stops listening to the pureplate endpoint.
   */
  @AfterEach
  public void tearDown() {
    Spark.unmap("/pureplate");
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

  // test one food, two foods, three foods
  // test bad request (wrong number of queries)
  // test bad datasource (empty queries, unrecognized foods)
//  this.foodData.put("Carrots, baby, raw", new HashMap<>());
//
//    this.foodData.put("Tomato, roma", new HashMap<>());
//
//    this.foodData.put("Pork, loin, boneless, raw", new HashMap<>());
  // pureplate?weight=10&height=10&age=10&gender=male&activity=very%20active&foods=Carrots,%20baby,%20raw`Tomato,%20roma
  @Test
  public void testPurePlateBasic() throws IOException {
    HttpURLConnection loadConnection =
        tryRequest("pureplate?weight=10&height=10&age=10&gender=male&activity=very%20active&foods=Carrots,%20baby,%20raw`Tomato,%20roma");
    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> responseMap =
        this.adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    System.out.println(responseMap);
    assertEquals(2, responseMap.size());
    assertEquals(Set.of("result", "recommendations"), responseMap.keySet());
    assertEquals("success", responseMap.get("result"));
    assertNotEquals("[]", responseMap.get("recommendations"));

//    assertEquals("0.0", responseMap.get("broadband"));
//    assertEquals("orange county", responseMap.get("county"));
//    assertEquals("california", responseMap.get("state"));

    // Orange County, Virginia
//    loadConnection = tryRequest("broadband?state=Virginia&county=Orange%20County");
//    assertEquals(200, loadConnection.getResponseCode());
//    responseMap = this.adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
//    assertEquals(5, responseMap.size());
//    assertEquals(
//        Set.of("result", "date & time", "broadband", "county", "state"), responseMap.keySet());
//    assertEquals("success", responseMap.get("result"));
//    assertEquals("0.0", responseMap.get("broadband"));
//    assertEquals("Orange County", responseMap.get("county"));
//    assertEquals("Virginia", responseMap.get("state"));

    loadConnection.disconnect();
  }

  @Test
  public void testPurePlateBadRequest() throws IOException {
//    HttpURLConnection loadConnection =
//        tryRequest("broadband?state=california&county=orange%20county&random=r");
//    assertEquals(200, loadConnection.getResponseCode());
//    Map<String, Object> responseMap =
//        this.adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
//    assertEquals(5, responseMap.size());
//    assertEquals("error_bad_json", responseMap.get("result"));
//    assertEquals("unrecognized parameter inputs provided", responseMap.get("message"));
//    assertEquals("orange county", responseMap.get("county"));
//    assertEquals("california", responseMap.get("state"));
//    assertEquals("r", responseMap.get("random"));
//    loadConnection.disconnect();
  }

  @Test
  public void testPurePlateBadDatasource() throws IOException {
    // No URI parameters
//    HttpURLConnection loadConnection = tryRequest("broadband");
//    assertEquals(200, loadConnection.getResponseCode());
//    Map<String, Object> responseMap =
//        this.adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
//    assertEquals(4, responseMap.size());
//    assertEquals("error_bad_request", responseMap.get("result"));
//    assertEquals("missing state and county parameters", responseMap.get("message"));
//    assertEquals("none", responseMap.get("county"));
//    assertEquals("none", responseMap.get("state"));
//
//    loadConnection.disconnect();
  }
}
