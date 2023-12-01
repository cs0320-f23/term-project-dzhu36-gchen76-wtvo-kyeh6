package edu.brown.cs.student.mapserver.datasources;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.mapserver.key.apikey;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import okio.Buffer;

public class NutritionDataSource implements Query<String, String>{

  /**
   * Stores the data for state codes. If this is null, there will be a call to the API to get these
   */
  HashMap<String, String> statesWithCode = null;

  public String query(String target) throws IOException {
    Moshi moshi = new Moshi.Builder().build();
    Type listStrings = Types.newParameterizedType(List.class, List.class, String.class);
    JsonAdapter<List<List<String>>> jsonAdapter = moshi.adapter(listStrings);
    URL requestUrl = new URL(target);
    HttpURLConnection clientConnection = (HttpURLConnection) requestUrl.openConnection();
    clientConnection.connect();
    List<List<String>> dataList =
        jsonAdapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
    List<String> targetRow = dataList.get(1);
    String broadband = targetRow.get(1);
    return broadband;
  }

  /**
   * Private helper method; throws IOException so different callers can handle differently if
   * needed.
   */
  private static HttpURLConnection connect(URL requestURL) throws DatasourceException, IOException {
    URLConnection urlConnection = requestURL.openConnection();
    if (!(urlConnection instanceof HttpURLConnection)) {
      throw new DatasourceException("unexpected: result of connection wasn't HTTP");
    }
    HttpURLConnection clientConnection = (HttpURLConnection) urlConnection; // may need to typecast
    clientConnection.connect();
    if (clientConnection.getResponseCode() != 200) {
      throw new DatasourceException(
          "unexpected: API connection not success status " + clientConnection.getResponseMessage());
    }
    return clientConnection;
  }

  /**
   * Given a state and county (one string), find the broadband percentage at that location by
   * invoking the US Census ACS API.
   *
   * @param stateCounty the state and county as one string to allow for cache loading.
   * @return the broadband percentage of a given county and state.
   * @throws DatasourceException if an error is encountered when getting data from the API.
   */
  public List<List<String>> getNutritionData(List<String> listFoods) throws DatasourceException {
    try (Buffer buffer = new Buffer()) {
      Moshi moshi = new Moshi.Builder().build();
      Type listString = Types.newParameterizedType(List.class, List.class, String.class);
      JsonAdapter<List<List<String>>> adapter = moshi.adapter(listString);
      HttpURLConnection clientConnection;
      // Find the state code from the state name
      if (listFoods.size() == 0) {
        throw new DatasourceException("Food list empty");
      }
      for (String food : listFoods) {
        food = food.replace(" ", "%20");
        URL foodURL =
            new URL(
                "https",
                "api.nal.usda.gov",
                "/fdc/v1/foods/search?dataType=Foundation&sortBy=dataType.keyword&sortOrder=asc&api_key=" + apikey.getKey() + "&query=" + food);
        clientConnection = connect(foodURL);
      }
      // find county code from county and state name
      URL countyCodeURL =
          new URL(
              "https",
              "api.census.gov",
              "/data/2010/dec/sf1?get=NAME&for=county:*&in=state:" + stateCode);
      clientConnection = connect(countyCodeURL);
      List<List<String>> counties =
          adapter.fromJson(buffer.readFrom(clientConnection.getInputStream()));
      String countyCode = "-1";
      for (int i = 1; i < counties.size(); i++) {
        if (counties.get(i).get(0).equals(stateAndCounty[1] + ", " + stateAndCounty[0])) {
          countyCode = counties.get(i).get(2);
        }
      }
      // final request
      URL requestURL =
          new URL(
              "https",
              "api.census.gov",
              "/data/2021/acs/acs1/subject/variables?"
                  + "get=NAME,S2802_C03_022E&for=county:"
                  + countyCode
                  + "&in=state:"
                  + stateCode);
      clientConnection = connect(requestURL);
      List<List<String>> body =
          adapter.fromJson(buffer.readFrom(clientConnection.getInputStream()));
      clientConnection.disconnect();
      if (body != null) {
        body.remove(0);
      }
      return body;
    } catch (Exception e) {
      throw new DatasourceException(e.getMessage());
    }
  }

  private double calculateBMR(
      int weight_kg, int height_cm, int age_years, String gender, String activity) {
    double caloricRequirement = 0;
    if (gender.toLowerCase() == "male") {
      caloricRequirement = 10 * weight_kg + 6.25 * height_cm - 5 * age_years + 5;
    } else if (gender.toLowerCase() == "female") {
      caloricRequirement = 10 * weight_kg + 6.25 * height_cm - 5 * age_years - 161;
    }
    switch(activity) {
      case "Sedentary":
        return caloricRequirement * 1.2;
      case "Lightly Active":
        return caloricRequirement * 1.375;
      case "Moderately Active":
        return caloricRequirement * 1.55;
      case "Very Active":
        return caloricRequirement * 1.725;
      case "Extra Active":
        return caloricRequirement * 1.9;
      default:
        return caloricRequirement;
    }
  }


}
