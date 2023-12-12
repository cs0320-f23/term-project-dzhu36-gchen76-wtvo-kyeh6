package edu.brown.cs.student.pureplate;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.pureplate.datasources.Query;
import java.util.HashMap;
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

   private static Query<String, String> cache;

  /**
   * Constructor for PurePlateHandler.
   */
  public PurePlateHandler(Query<String, String> myCache) {
    this.cache = myCache;
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
    Set<String> params = request.queryParams();
    Map<String, Object> results = new HashMap<>();
    results.put("test key", "test value");
//    for(String param : params) {
//      results.put(param, request.queryParams(param));
//    }
    results.put("recommendations", this.cache.query("HI"));

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
