package edu.brown.cs.student.mapserver;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.mapserver.datasources.Query;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * A class that handles GeoJson redlining data filtering based on user-inputted endpoint URI
 * parameters.
 */
public class MapsHandler implements Route {

  private static Query<String, String> cache;

  /**
   * Constructor for MapsHandler.
   *
   * @param jsonData - a Query instance that retrieves and stores the GeoJson redlining data.
   */
  public MapsHandler(Query<String, String> jsonData) {
    cache = jsonData;
  }

  /**
   * This method handles when the geodata endpoint is visited.
   *
   * @param request  The request object providing information about the HTTP request
   * @param response The response object providing functionality for modifying the response
   * @return a serialized Map that contains the necessary output the user will see (e.g., the
   * filtered GeoJson redlining data).
   * @throws Exception if any Exception is thrown in the process.
   */
  @Override
  public Object handle(Request request, Response response) throws Exception {
    Set<String> params = request.queryParams();
    Map<String, Object> results = new HashMap<>();
    String minLat = request.queryParams("min_lat");
    String maxLat = request.queryParams("max_lat");
    String minLong = request.queryParams("min_long");
    String maxLong = request.queryParams("max_long");
    String keyword = request.queryParams("keyword");

    if (params.isEmpty()) {
      results.put("result", "success");
      results.put("geo_data", cache.query(""));
      return this.serialize(results);
    } else if (keyword != null && params.size() == 1) {
      results.put("result", "success");
      results.put("keyword", keyword);
      results.put("geo_data", cache.query(keyword));
      return this.serialize(results);
    } else if (minLat == null || maxLat == null || minLong == null || maxLong == null
        || params.size() > 4) {
      results.put("result", "error_bad_request");
      return this.serialize(results);
    } else {
      try {
        double minLatNum = Double.parseDouble(minLat);
        double maxLatNum = Double.parseDouble(maxLat);
        double minLongNum = Double.parseDouble(minLong);
        double maxLongNum = Double.parseDouble(maxLong);
        if (minLatNum > maxLatNum || minLongNum > maxLongNum || minLatNum < -90 || maxLatNum > 90
            || minLongNum < -180 || maxLongNum > 180) {
          results.put("result", "error_bad_request");
          return this.serialize(results);
        }
      } catch (NumberFormatException e) {
        results.put("result", "error_bad_request");
        return this.serialize(results);
      }
      results.put("result", "success");
      results.put("geo_data",
          cache.query(minLat + "," + maxLat + "," + minLong + "," + maxLong));
      return this.serialize(results);
    }
  }

  /**
   * Serializes an inputted Map into json format.
   *
   * @param contents - the to-be-serialized Map.
   * @return the serialized Map.
   */
  private String serialize(Map<String, Object> contents) {
    Moshi moshi = new Moshi.Builder().build();
    JsonAdapter<Map<String, Object>> adapter =
        moshi.adapter(Types.newParameterizedType(Map.class, String.class, Object.class));
    return adapter.toJson(contents);
  }
}
