package edu.brown.cs.student.broadbandtests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.mapserver.BroadBandHandler;
import edu.brown.cs.student.mapserver.datasources.Query;
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
 * Tests the functionality of BroadBandHandler.
 */
public class TestBroadBandHandler {

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
   * Sets up the broadband endpoint.
   */
  @BeforeEach
  public void setup() {
    Query<String, String> mockedSource = new MockBroadbandQuery();
    Spark.get("/broadband", new BroadBandHandler(mockedSource));
    Spark.awaitInitialization();
    Moshi moshi = new Moshi.Builder().build();
    Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
    this.adapter = moshi.adapter(mapStringObject);
  }

  /**
   * Stops listening to the broadband endpoint.
   */
  @AfterEach
  public void tearDown() {
    Spark.unmap("/broadband");
    Spark.awaitStop();
  }

  /**
   * Helper to start a connection to a specific API endpoint. This method is mostly borrowed from
   * the TestSoupAPIHandlers class in the server gear-up program.
   *
   * @param apiCall - the URI for the API endpoint
   * @return the connection for the given URI, just after connecting
   * @throws IOException if the connection fails for some reason
   */
  private HttpURLConnection tryRequest(String apiCall) throws IOException {
    URL requestURL = new URL("http://localhost:" + Spark.port() + "/" + apiCall);
    HttpURLConnection clientConnection = (HttpURLConnection) requestURL.openConnection();
    clientConnection.connect();
    return clientConnection;
  }

  /**
   * Tests that broadband data can be retrieved when a valid state and county identifier are
   * provided.
   *
   * @throws IOException if an error occurs with the URI connection.
   */
  @Test
  public void testBroadbandBasic() throws IOException {
    // Orange County, California
    HttpURLConnection loadConnection =
        tryRequest("broadband?state=california&county=orange%20county");
    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> responseMap =
        this.adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertEquals(5, responseMap.size());
    assertEquals(
        Set.of("result", "date & time", "broadband", "county", "state"), responseMap.keySet());
    assertEquals("success", responseMap.get("result"));
    assertEquals("0.0", responseMap.get("broadband"));
    assertEquals("orange county", responseMap.get("county"));
    assertEquals("california", responseMap.get("state"));

    // Orange County, Virginia
    loadConnection = tryRequest("broadband?state=Virginia&county=Orange%20County");
    assertEquals(200, loadConnection.getResponseCode());
    responseMap = this.adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertEquals(5, responseMap.size());
    assertEquals(
        Set.of("result", "date & time", "broadband", "county", "state"), responseMap.keySet());
    assertEquals("success", responseMap.get("result"));
    assertEquals("0.0", responseMap.get("broadband"));
    assertEquals("Orange County", responseMap.get("county"));
    assertEquals("Virginia", responseMap.get("state"));

    loadConnection.disconnect();
  }

  /**
   * Tests that the endpoint gives the user a bad json message when the request is ill-formed.
   *
   * @throws IOException if an error occurs with the URI connection
   */
  @Test
  public void testBroadbandBadJson() throws IOException {
    HttpURLConnection loadConnection =
        tryRequest("broadband?state=california&county=orange%20county&random=r");
    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> responseMap =
        this.adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertEquals(5, responseMap.size());
    assertEquals("error_bad_json", responseMap.get("result"));
    assertEquals("unrecognized parameter inputs provided", responseMap.get("message"));
    assertEquals("orange county", responseMap.get("county"));
    assertEquals("california", responseMap.get("state"));
    assertEquals("r", responseMap.get("random"));
    loadConnection.disconnect();
  }

  /**
   * Tests that the endpoint gives the user a bad request message when parameters are provided
   * incorrectly.
   *
   * @throws IOException if an error occurs with the URI connection.
   */
  @Test
  public void testBroadbandBadRequest() throws IOException {
    // No URI parameters
    HttpURLConnection loadConnection = tryRequest("broadband");
    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> responseMap =
        this.adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertEquals(4, responseMap.size());
    assertEquals("error_bad_request", responseMap.get("result"));
    assertEquals("missing state and county parameters", responseMap.get("message"));
    assertEquals("none", responseMap.get("county"));
    assertEquals("none", responseMap.get("state"));

    // No state parameter
    loadConnection = tryRequest("broadband?county=orange%20county");
    assertEquals(200, loadConnection.getResponseCode());
    responseMap = this.adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertEquals(4, responseMap.size());
    assertEquals("error_bad_request", responseMap.get("result"));
    assertEquals("missing state or county parameter", responseMap.get("message"));
    assertEquals("orange county", responseMap.get("county"));
    assertEquals("none", responseMap.get("state"));

    // No county parameter
    loadConnection = tryRequest("broadband?state=california");
    assertEquals(200, loadConnection.getResponseCode());
    responseMap = this.adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertEquals(4, responseMap.size());
    assertEquals("error_bad_request", responseMap.get("result"));
    assertEquals("missing state or county parameter", responseMap.get("message"));
    assertEquals("none", responseMap.get("county"));
    assertEquals("california", responseMap.get("state"));

    // Nonexistent county identifier
    loadConnection = tryRequest("broadband?state=california&county=nonexistent");
    assertEquals(200, loadConnection.getResponseCode());
    responseMap = this.adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertEquals(4, responseMap.size());
    assertEquals("error_bad_request", responseMap.get("result"));
    assertEquals("unrecognized county identifier nonexistent", responseMap.get("message"));
    assertEquals("nonexistent", responseMap.get("county"));
    assertEquals("california", responseMap.get("state"));

    // Nonexistent state parameter
    loadConnection = tryRequest("broadband?state=nonexistent&county=orange%20county");
    assertEquals(200, loadConnection.getResponseCode());
    responseMap = this.adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertEquals(4, responseMap.size());
    assertEquals("error_bad_request", responseMap.get("result"));
    assertEquals("unrecognized state identifier nonexistent", responseMap.get("message"));
    assertEquals("orange county", responseMap.get("county"));
    assertEquals("nonexistent", responseMap.get("state"));

    loadConnection.disconnect();
  }
}
