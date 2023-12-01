package edu.brown.cs.student.mapserver;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.mapserver.datasources.Query;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import okio.Buffer;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * A class that handles broadband percentage fetching from the broadband endpoint.
 */
public class BroadBandHandler implements Route {

  private static Map<String, String> states = new HashMap<>();
  private static Query<String, String> cache;

  /**
   * Constructor for BroadBandHandler.
   *
   * @param apiData - a Query instance that queries to the ACS API
   */
  public BroadBandHandler(Query<String, String> apiData) {
    cache = apiData;
  }

  /**
   * This method handles (i.e., executes specific actions) when the broadband endpoint is accessed.
   *
   * @param request  - the request object providing information about the HTTP request
   * @param response - the response object providing functionality for modifying the response
   * @return an Object to be depicted on the user's end when the endpoint is accessed
   * @throws Exception if any Exception is thrown in the process
   */
  @Override
  public Object handle(Request request, Response response) throws Exception {
    Set<String> params = request.queryParams();
    Map<String, Object> results = new HashMap<>();

    String state = request.queryParams("state");
    String county = request.queryParams("county");
    for (String param : params) {
      results.put(param, request.queryParams(param));
    }
    if (state == null) {
      results.put("state", "none");
    }
    if (county == null) {
      results.put("county", "none");
    }

    if (params.isEmpty()) {
      results.put("result", "error_bad_request");
      results.put("message", "missing state and county parameters");
      return this.serialize(results);
    }

    if ((state == null) || (county == null)) {
      results.put("result", "error_bad_request");
      results.put("message", "missing state or county parameter");
      return this.serialize(results);
    } else if (params.size() > 2) {
      results.put("result", "error_bad_json");
      results.put("message", "unrecognized parameter inputs provided");
      return this.serialize(results);
    }

    Moshi moshi = new Moshi.Builder().build();
    Type listStrings = Types.newParameterizedType(List.class, List.class, String.class);
    JsonAdapter<List<List<String>>> jsonAdapter = moshi.adapter(listStrings);
    if (this.states.isEmpty()) {
      HttpURLConnection clientConnection =
          this.tryRequest("https://api.census.gov/data/2010/dec/sf1?get=NAME&for=state:*");
      // Retrieving states
      List<List<String>> statesList =
          jsonAdapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
      for (List<String> currRow : statesList) {
        this.states.put(currRow.get(0).toLowerCase(), currRow.get(1));
      }
    }

    String stateIdentifier = this.states.get(state.toLowerCase());
    if (stateIdentifier == null) {
      results.put("result", "error_bad_request");
      results.put("message", "unrecognized state identifier " + state);
      return this.serialize(results);
    }

    // Retrieving counties
    HttpURLConnection clientConnection =
        this.tryRequest(
            "https://api.census.gov/data/2010/dec/sf1?get=NAME&for=county:*&in=state:"
                + stateIdentifier);
    List<List<String>> countiesList =
        jsonAdapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
    Map<String, String> counties = new HashMap<>();
    for (List<String> currRow : countiesList) {
      counties.put(currRow.get(0).split("[,]", 0)[0].toLowerCase(), currRow.get(2));
    }

    String countyIdentifier = counties.get(county.toLowerCase());
    if (countyIdentifier == null) {
      results.put("result", "error_bad_request");
      results.put("message", "unrecognized county identifier " + county);
      return this.serialize(results);
    }

    // Retrieving broadband data
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    LocalDateTime now = LocalDateTime.now();

    try {
      String broadband =
          cache.query(
              "https://api.census.gov/data/2021/acs/acs1/subject/variables?get=NAME,S2802_C03_022E&for=county:"
                  + countyIdentifier
                  + "&in=state:"
                  + stateIdentifier);
      results.put("result", "success");
      results.put("broadband", broadband);
      results.put("date & time", dtf.format(now));
    } catch (Exception e) {
      results.put("result", "error_bad_json");
      results.put("message", "broadband data unavailable for " + county + ", " + state);
    }

    clientConnection.disconnect();
    return this.serialize(results);
  }

  /**
   * A helper method that attempts to establish a connection with a URI. This method is mostly
   * borrowed from the TestSoupAPIHandlers class in the server gear-up program.
   *
   * @param apiCall - the String representation of a URI
   * @return a connection with the apiCall represented as a HttpURLConnection instance
   * @throws IOException if an error occurs when connecting with the URI
   */
  private HttpURLConnection tryRequest(String apiCall) throws IOException {
    URL requestUrl = new URL(apiCall);
    HttpURLConnection clientConnection = (HttpURLConnection) requestUrl.openConnection();
    clientConnection.connect();
    return clientConnection;
  }

  /**
   * Serializes an inputted Map into json format.
   *
   * @param results - the Map to be serialized
   * @return a String json representation of the results Map.
   */
  private String serialize(Map<String, Object> results) {
    Moshi moshi = new Moshi.Builder().build();
    return moshi.adapter(Map.class).toJson(results);
  }
}
