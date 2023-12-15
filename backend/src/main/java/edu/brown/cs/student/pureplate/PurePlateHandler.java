package edu.brown.cs.student.pureplate;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.pureplate.datasources.DatasourceException;
import edu.brown.cs.student.pureplate.datasources.Query;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * A class that handles calculating nutritional deficits and suggestions, and then depicting the
 * output data on the pureplate endpoint, based on input data.
 */
public class PurePlateHandler implements Route {

   private static Query<String, List<String>> cache;

  /**
   * Constructor for PurePlateHandler.
   */
  public PurePlateHandler(Query<String, List<String>> myCache) {
    cache = myCache;
  }

  /**
   * This method handles when the pureplate endpoint is visited.
   *
   * @param request  The request object providing information about the HTTP request
   * @param response The response object providing functionality for modifying the response
   * @return a serialized Map that contains the necessary output the user will see.
   * @throws Exception if any Exception is thrown in the process.
   */
  @Override
  public Object handle(Request request, Response response) throws Exception {
    Set<String> params = request.queryParams(); // might not use
    Map<String, Object> results = new HashMap<>();
    String weight = request.queryParams("weight");
    String height = request.queryParams("height");
    String age = request.queryParams("age");
    String gender = request.queryParams("gender");
    String activity = request.queryParams("activity");
    String foods = request.queryParams("foods");

    if (weight == null || height == null || age == null || gender == null || activity == null || foods == null) {
      results.put("result", "error_bad_request");
      results.put("message", "missing request parameter");
      return this.serialize(results);
    }

    try {
      results.put("recommendations", cache.query(List.of(weight, height, age, gender, activity, foods)));
      results.put("result", "success");
    } catch (Exception e) {
      System.out.println(e.getMessage());
      results.put("result", "error_bad_datasource");
      results.put("message", "recommendations could not be retrieved");
    }

    return this.serialize(results);
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
