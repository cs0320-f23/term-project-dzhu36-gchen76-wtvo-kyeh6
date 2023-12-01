package edu.brown.cs.student.geodatatests;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.pureplate.PurePlateHandler;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
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
    Spark.get("/pureplate", new PurePlateHandler());
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
}
