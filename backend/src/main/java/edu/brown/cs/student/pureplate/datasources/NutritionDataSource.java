package edu.brown.cs.student.pureplate.datasources;

import com.squareup.moshi.Json;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.pureplate.key.apikey;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

import okio.Buffer;

public class NutritionDataSource implements Query<String, String> {

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
    return targetRow.get(1);
  }

  /**
   * Private helper method; throws IOException so different callers can handle differently if
   * needed.
   */
  private static HttpURLConnection connect(URL requestURL) throws DatasourceException, IOException {
    URLConnection urlConnection = requestURL.openConnection();
    if (!(urlConnection instanceof HttpURLConnection clientConnection)) {
      throw new DatasourceException("unexpected: result of connection wasn't HTTP");
    }
    // may need to typecast
    clientConnection.connect();
    if (clientConnection.getResponseCode() != 200) {
      throw new DatasourceException(
              "unexpected: API connection not success status " + clientConnection.getResponseMessage());
    }
    return clientConnection;
  }

  public Map<String, List<FoodNutrient>> getNutritionData(List<String> listFoods) throws DatasourceException {
    try (Buffer buffer = new Buffer()) {
      Moshi moshi = new Moshi.Builder().build();
      JsonAdapter<FoodDataResponse> adapter = moshi.adapter(FoodDataResponse.class);
      Map<String, List<FoodNutrient>> returnMap = new HashMap<>();
      HttpURLConnection clientConnection;
      if (listFoods.isEmpty()) {
        throw new DatasourceException("Food list empty");
      }
      for (String food : listFoods) {
        URL foodURL =
                new URL(
                        "https",
                        "api.nal.usda.gov",
                        "/fdc/v1/foods/search?dataType=Foundation" +
                                "&sortBy=dataType.keyword" +
                                "&pageSize=1" +
                                "&sortOrder=asc" +
                                "&api_key=" + apikey.getKey() +
                                "&query=" + food.replace(" ", "%20"));
        clientConnection = connect(foodURL);
        FoodDataResponse response = adapter.fromJson(buffer.readFrom(clientConnection.getInputStream()));
        if (response == null) {
          continue;
        } else if (response.foods == null || response.foods.isEmpty()) { //response.foods.get(0) == null
          returnMap.put(food, new ArrayList<>());
        } else {
          returnMap.put(response.foods.get(0).description, response.foods.get(0).foodNutrients);
        }
        clientConnection.disconnect();
      }
      return returnMap;
    } catch (Exception e) {
      throw new DatasourceException(e.getMessage());
    }
  }

  private List<FoodAbridged> getFoodDatabase() throws DatasourceException{
    try (Buffer buffer = new Buffer()) {
      Moshi moshi = new Moshi.Builder().build();
      JsonAdapter<List<FoodAbridged>> adapter = moshi.adapter(Types.newParameterizedType(List.class, FoodAbridged.class));
      HttpURLConnection clientConnection;
        URL foodListURL =
                new URL(
                        "https",
                        "api.nal.usda.gov",
                        "/fdc/v1/foods/list?dataType=Foundation" +
                                "&pageSize=200" +
                                "&api_key=" + apikey.getKey());
        clientConnection = connect(foodListURL);
        List<FoodAbridged> returnList = adapter.fromJson(buffer.readFrom(clientConnection.getInputStream()));
        if (returnList == null) {
          throw new DatasourceException("Could not connect");
        }
        foodListURL =
              new URL(
                      "https",
                      "api.nal.usda.gov",
                      "/fdc/v1/foods/list?dataType=Foundation" +
                              "&pageSize=200" +
                              "&pageNumber=2" +
                              "&api_key=" + apikey.getKey());
        clientConnection = connect(foodListURL);
        List<FoodAbridged> returnList2 = adapter.fromJson(buffer.readFrom(clientConnection.getInputStream()));
        if (returnList2 == null) {
          throw new DatasourceException("Could not connect");
        }
        returnList.addAll(returnList2);
        clientConnection.disconnect();
        return returnList;
    }
    catch (Exception e) {
      throw new DatasourceException(e.getMessage());
    }
  }

  // public for testing purposes
  public double calculateCaloricRequirement(
          int weight_kg, int height_cm, int age_years, String gender, String activity)
      throws DatasourceException {
    if (weight_kg < 0 || height_cm < 0 || age_years < 0) {
      throw new DatasourceException("Measurement is negative");
    }
    double caloricRequirement = 0;
    if (gender.equalsIgnoreCase("male")) {
      caloricRequirement = 10 * weight_kg + 6.25 * height_cm - 5 * age_years + 5;
    } else if (gender.equalsIgnoreCase("female")) {
      caloricRequirement = 10 * weight_kg + 6.25 * height_cm - 5 * age_years - 161;
    }
    return switch (activity) {
      case "Sedentary" -> caloricRequirement * 1.2;
      case "Lightly Active" -> caloricRequirement * 1.375;
      case "Moderately Active" -> caloricRequirement * 1.55;
      case "Very Active" -> caloricRequirement * 1.725;
      case "Extra Active" -> caloricRequirement * 1.9;
      default -> caloricRequirement;
    };
  }

  public record FoodSearchCriteria(
          @Json(name = "dataType") List<String> dataType,
          @Json(name = "query") String query,
          @Json(name = "generalSearchInput") String generalSearchInput,
          @Json(name = "pageNumber") int pageNumber,
          @Json(name = "sortBy") String sortBy,
          @Json(name = "sortOrder") String sortOrder,
          @Json(name = "numberOfResultsPerPage") int numberOfResultsPerPage,
          @Json(name = "pageSize") int pageSize,
          @Json(name = "requireAllWords") boolean requireAllWords,
          @Json(name = "foodTypes") List<String> foodTypes
  ) {
  }

  public record FoodNutrient(
          @Json(name = "nutrientId") int nutrientId,
          @Json(name = "nutrientName") String nutrientName,
          @Json(name = "nutrientNumber") String nutrientNumber,
          @Json(name = "unitName") String unitName,
          @Json(name = "derivationCode") String derivationCode,
          @Json(name = "derivationDescription") String derivationDescription,
          @Json(name = "derivationId") int derivationId,
          @Json(name = "value") double value,
          @Json(name = "foodNutrientSourceId") int foodNutrientSourceId,
          @Json(name = "foodNutrientSourceCode") String foodNutrientSourceCode,
          @Json(name = "foodNutrientSourceDescription") String foodNutrientSourceDescription,
          @Json(name = "rank") int rank,
          @Json(name = "indentLevel") int indentLevel,
          @Json(name = "foodNutrientId") int foodNutrientId
          //@Json(name = "dataPoints") int dataPoints
  ) {
  }

  public record Food(
          @Json(name = "fdcId") int fdcId,
          @Json(name = "description") String description,
          @Json(name = "commonNames") String commonNames,
          @Json(name = "additionalDescriptions") String additionalDescriptions,
          @Json(name = "dataType") String dataType,
          @Json(name = "ndbNumber") int ndbNumber,
          @Json(name = "publishedDate") String publishedDate,
          @Json(name = "foodCategory") String foodCategory,
          @Json(name = "allHighlightFields") String allHighlightFields,
          @Json(name = "score") double score,
          @Json(name = "microbes") List<String> microbes,
          @Json(name = "foodNutrients") List<FoodNutrient> foodNutrients
  ) {
  }

  public record FoodDataResponse(
          @Json(name = "totalHits") int totalHits,
          @Json(name = "currentPage") int currentPage,
          @Json(name = "totalPages") int totalPages,
          @Json(name = "pageList") List<Integer> pageList,
          @Json(name = "foodSearchCriteria") FoodSearchCriteria foodSearchCriteria,
          @Json(name = "foods") List<Food> foods
  ) {
  }

  public record FoodAbridged(
          @Json(name = "fdcId") int fdcId,
          @Json(name = "description") String description,
          @Json(name = "dataType") String dataType,
          @Json(name = "publicationDate") String publicationDate,
          @Json(name = "ndbNumber") String ndbNumber,
          @Json(name = "foodNutrients") List<FoodNutrientAbridged> foodNutrientsAbridged
  ) {
  }

  public record FoodNutrientAbridged(
          @Json(name = "number") String number,
          @Json(name = "name") String name,
          @Json(name = "amount") double amount,
          @Json(name = "unitName") String unitName,
          @Json(name = "derivationCode") String derivationCode,
          @Json(name = "derivationDescription") String derivationDescription
  ) {
  }
}
