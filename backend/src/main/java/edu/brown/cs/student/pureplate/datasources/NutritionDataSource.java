package edu.brown.cs.student.pureplate.datasources;

import com.squareup.moshi.Json;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.pureplate.key.apikey;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

import okio.Buffer;

public class NutritionDataSource implements Query<String, List<String>> {

  /**
   * Stores the data for nutritional guidelines mapping from gender to a hashmap of guidelines
   */
  private static Map<String, Map<String, Double>> nutritionalRequirements;
  private Map<String, Double> nutritionNeeds;
  private Map<String, Map<String, Double>> foodData;
  private List<String> visited;
  private final List<String> growable = parseGrowable();
  private boolean onlyGrowable = false;
  public NutritionDataSource(String filename) {
    try {
      this.foodData = new HashMap<>();
      this.nutritionNeeds = new HashMap<>();
      this.getFoodDatabase();
      CsvParser parser = new CsvParser();
      parser.parse(filename); // used to be data/nutrition/etc...
      nutritionalRequirements = parser.getTable();
      System.out.println(nutritionalRequirements);
    } catch (DatasourceException e) {
      nutritionalRequirements = new HashMap<>();
    }
  }

  /**
   *
   * @param target - the URI of the ACS API with state and county identifiers (or in the context of
   *               the redlining data, a type that contains all the parameters to filter the
   *               redlining data on)
   * @return
   * @throws DatasourceException
   */
  public String query(List<String> target) throws DatasourceException {
    // note to self: check case where one of these empty
    String weight = target.get(0);
    String height = target.get(1);
    String age = target.get(2);
    String gender = target.get(3);
    String activity = target.get(4);
    String growable = target.get(5);
    String foodsString = target.get(6);

//    System.out.println("query before functions");
    this.visited = new ArrayList<>();
    List<String> foodsList = Arrays.asList(foodsString.split("`"));
    this.visited.addAll(foodsList);
    if (growable.equalsIgnoreCase("yes")) {
      this.onlyGrowable = true;
      System.out.println("yes");
    } else if (!growable.equalsIgnoreCase("no")) {
      System.out.println("not no");
      throw new DatasourceException("Unreasonable growable parameter");
    }
    System.out.println("no");
//      // Calculate Caloric Requirement
//      // Update nutritionNeeds based on the article and guidelines

    try {
      this.calculateRatios(this.calculateCaloricRequirement(Double.parseDouble(weight), Integer.parseInt(height), Integer.parseInt(age), gender, activity), gender);
    } catch (NumberFormatException e) {
      throw new DatasourceException("Unreasonable weight, height, or age parameter");
    }
    this.calculateDeficiency(this.visited);
    return this.getRecommendations().toString();



//    this.calculateRatios(2000, "Male");
//    System.out.println("after calculating ratios");
////      // Calculate Deficiencies
////    this.calculateRatios(this.calculateCaloricRequirement(Double.parseDouble(foods.get(0)), Integer.parseInt(foods.get(1)), Integer.parseInt(foods.get(2)), foods.get(3), foods.get(4)), foods.get(3));
////    for (String food : foodSet) {
////      for (String key : this.foodData.get(food).keySet()) {
////        this.nutritionNeeds.put(key, this.nutritionNeeds.get(key) - this.foodData.get(food).get(key));
////      }
////    }
//      this.calculateDeficiency(this.visited);
//      System.out.println("query after deficiency");
//      return this.getRecommendations().toString();
  }

  private Map<String, Double> getScore() {
    Map<String, Double> scoreMap = new HashMap<>();
    for (String foodKey : this.foodData.keySet()) {
      double score = 0.0;
      int counter = 0;
//      for (String key : this.foodData.get(foodKey).keySet()) {
      for (String key : this.nutritionNeeds.keySet()) {
        if (this.nutritionNeeds.get(key) > 0.0) {
          double nutrient = 0;
          for (String foodNutrients : this.foodData.get(foodKey).keySet()) {
            if (foodNutrients.toLowerCase().contains(key.toLowerCase())) {
              nutrient += this.foodData.get(foodKey).get(foodNutrients);
            }
          }
          score -= 1 / (1 + Math.max(0.0,
              this.nutritionNeeds.get(key) - nutrient));
          counter++;
//          if (key.equals("Vitamin D")) {
//            System.out.println("Vitamin D Value: " + nutrient);
//          }
//          if (key.equals("Vitamin K")) {
//            System.out.println("Vitamin K Value: " + nutrient);
//          }
        }
      }
      if (this.onlyGrowable) {
        scoreMap.put(foodKey, counter > 0 ? ((this.visited.contains(foodKey) ? 50.0 * -(counter / score) : (!this.growable.contains(foodKey) ? 1000.00 * -(counter / score) : score / counter ))) : 100.0); // if the food doesn't have the nutrient
      } else {
        scoreMap.put(foodKey, counter > 0 ? (this.visited.contains(foodKey) ? 50.0 * -(counter / score) : score / counter) : 100.0);
      }
    }
    return scoreMap;
  }

  private List<String> getRecommendations() {
    List<String> recommendationList = new ArrayList<>();
    Map<String, Double> scoreMap = this.getScore();
    // populate priority queue using getScore
    //System.out.println("Priority Queue");
    PriorityQueue<String> priorityFoods = new PriorityQueue<>(
        Comparator.comparingDouble(scoreMap::get));
    //System.out.println("Recommendation before while loop");
    while (!this.nutritionNeeds.entrySet().stream()
        .filter(entry -> !entry.getKey().equals("Calorie Level Assessed"))
        .allMatch(entry -> entry.getValue() <= 0.0)){
      //System.out.println("Beginning of while loop");
      // add all foods to priority queue and pull the first food out and add to returnlist
      priorityFoods.addAll(this.foodData.keySet());
      
      // reupdate nutritional needs based off that food
      String highestPriorityFood = priorityFoods.peek();
      //System.out.println("replacing nutrition needs: " + highestPriorityFood);

//      this.nutritionNeeds.replaceAll((n, v) -> this.nutritionNeeds.get(n)
//          - this.foodData.get(highestPriorityFood).get(n));

      // mark the food as already recommended
      this.visited.add(highestPriorityFood);

      // reupdate nutritional needs based off that food
      this.calculateDeficiency(this.visited);
      System.out.println(nutritionNeeds);

      //System.out.println("Update score map");
      // update Scores
      scoreMap = this.getScore();

      // re-add everything to priority
      priorityFoods = new PriorityQueue<>(
          Comparator.comparingDouble(scoreMap::get));
      priorityFoods.addAll(this.foodData.keySet());
      recommendationList.add(highestPriorityFood);
    }
    return recommendationList;
  }

//  private Map<String, List<String>> getRecommendations() {
//    // for (String nutrientName:
//    // while loop until the nutrition Needs value is <= 0,
//    return new Map<>();
//  }
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



  // made public for unit testing
  public void getFoodDatabase() throws DatasourceException{
    try (Buffer buffer = new Buffer()) {
      Moshi moshi = new Moshi.Builder().build();
      JsonAdapter<List<Food>> adapter = moshi.adapter(Types.newParameterizedType(List.class, Food.class));
      HttpURLConnection clientConnection;
        URL foodListURL =
                new URL(
                        "https",
                        "api.nal.usda.gov",
                        "/fdc/v1/foods/list?dataType=Foundation" +
                                "&pageSize=200" +
                                "&api_key=" + apikey.getKey());
        clientConnection = connect(foodListURL);
        List<Food> returnList = adapter.fromJson(buffer.readFrom(clientConnection.getInputStream()));
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
        List<Food> returnList2 = adapter.fromJson(buffer.readFrom(clientConnection.getInputStream()));
        if (returnList2 == null) {
          throw new DatasourceException("Could not connect");
        }
        returnList.addAll(returnList2);
        for (Food food : returnList) {
          Map<String, Double> nutrientAmount = new HashMap<>();
          for (FoodNutrient nutrient : food.foodNutrients) {
            nutrientAmount.put(nutrient.name, nutrient.amount);
          }
          this.foodData.put(food.description, nutrientAmount);
        }
        clientConnection.disconnect();
    }
    catch (IOException | NullPointerException e) {
      throw new DatasourceException(e.getMessage());
    }
  }

  // public for testing purposes
  /**
   * Calculates the Caloric Requirements based on input from the frontend
   * @param weight_kg
   * @param height_cm
   * @param age_years
   * @param gender
   * @param activity
   * @return
   * @throws DatasourceException
   */
  public double calculateCaloricRequirement(
          double weight_kg, int height_cm, int age_years, String gender, String activity)
      throws DatasourceException {
    if (weight_kg <= 0 || height_cm <= 0 || age_years <= 0) {
      throw new DatasourceException("Unrecognized parameter input");
    }
    double caloricRequirement = 0;
    if (gender.equalsIgnoreCase("male")) {
      caloricRequirement = 10 * weight_kg + 6.25 * height_cm - 5 * age_years + 5;
    } else if (gender.equalsIgnoreCase("female")) {
      caloricRequirement = 10 * weight_kg + 6.25 * height_cm - 5 * age_years - 161;
    } else {
      throw new DatasourceException("Unrecognized parameter input");
    }
    // Made case-insensitive
    return switch (activity.toLowerCase()) {
      case "sedentary" -> caloricRequirement * 1.2;
      case "lightly active" -> caloricRequirement * 1.375;
      case "moderately active" -> caloricRequirement * 1.55;
      case "very active" -> caloricRequirement * 1.725;
      case "extra active" -> caloricRequirement * 1.9;
      default -> caloricRequirement*=1.0;
    };
  }

  /**
   * Calculates the need for each nutrient per person based on standardized guidelines
   * @param caloricRequirements
   * @param gender
   */
  public void calculateRatios(double caloricRequirements, String gender) {
    // strings to be able to be converted to double, otherwise number format exception
    // check csv file
    gender = gender.toLowerCase();
    //System.out.println("parsing csv");
//    System.out.println(this.nutritionalRequirements);
//    if (this.nutritionalRequirements.get("Calorie Level Assessed") == null) {
//      System.out.println("cannot find calorie value");
//    }
//    System.out.println(nutritionalRequirements.keySet());
    double caloricRatio = caloricRequirements / nutritionalRequirements.get(gender).get("Calorie Level Assessed");
    //System.out.println(caloricRatio);
    for (String key : nutritionalRequirements.get(gender).keySet()) {
      this.nutritionNeeds.put(key, nutritionalRequirements.get(gender).get(key) / caloricRatio);
    }
  }

  public void calculateDeficiency(List<String> foods){
    //System.out.println("in calculatedeficiency");
    //System.out.println(this.nutritionNeeds);
    Set<String> foodSet = new HashSet<>(foods);
//    this.calculateRatios(this.calculateCaloricRequirement(Double.parseDouble(foods.get(0)), Integer.parseInt(foods.get(1)), Integer.parseInt(foods.get(2)), foods.get(3), foods.get(4)), foods.get(3));
    for (String food : foodSet) {
      //System.out.println("In outer for loop " + food);
//      for (String key : this.foodData.get(food).keySet()) {
      for (String key : this.nutritionNeeds.keySet()) {
        //System.out.println(this.nutritionNeeds);
        //System.out.println(this.foodData.get(food));

        // correctly assigns the nutrition value for the food
        double nutritionValue = 0;
        for (String foodNutrients : this.foodData.get(food).keySet()) {
          if (foodNutrients.toLowerCase().contains(key.toLowerCase())) {
            nutritionValue += this.foodData.get(food).get(foodNutrients);
          }
        }
//        if (key.equals("Vitamin D")) {
//          System.out.println("Deficiency Vitamin D: " + nutritionValue);
//        }
        this.nutritionNeeds.put(key, this.nutritionNeeds.get(key) - nutritionValue);
      }
    }
  }

  public static List<String> parseGrowable() {
    try {
      List<String> list = new ArrayList<>();
      BufferedReader bufferedReader = new BufferedReader(new FileReader("data/nutrition/growable.txt"));
      String currentFileLine = bufferedReader.readLine();
      while (currentFileLine != null) {
        list.add(currentFileLine);
        currentFileLine = bufferedReader.readLine();
      }
      bufferedReader.close();
      return list;
    } catch (IOException e) {
      return new ArrayList<>();
    }
  }

  public Map<String, Map<String, Double>> getFoodData () {
    return new HashMap<>(this.foodData);
  }

  public Map<String, Double> getNutritionNeeds() {
    return new HashMap<>(this.nutritionNeeds);
  }

  public record Food(
          @Json(name = "fdcId") int fdcId,
          @Json(name = "description") String description,
          @Json(name = "dataType") String dataType,
          @Json(name = "publicationDate") String publicationDate,
          @Json(name = "ndbNumber") String ndbNumber,
          @Json(name = "foodNutrients") List<FoodNutrient> foodNutrients
  ) {
  }

  public record FoodNutrient(
          @Json(name = "number") String number,
          @Json(name = "name") String name,
          @Json(name = "amount") Double amount,
          @Json(name = "unitName") String unitName,
          @Json(name = "derivationCode") String derivationCode,
          @Json(name = "derivationDescription") String derivationDescription
  ) {
    public FoodNutrient {
      if(amount == null) amount = 0.0;
    }
  }
}

//  public Map<String, List<FoodNutrient>> getNutritionData(List<String> listFoods) throws DatasourceException {
//    try (Buffer buffer = new Buffer()) {
//      Moshi moshi = new Moshi.Builder().build();
//      JsonAdapter<FoodDataResponse> adapter = moshi.adapter(FoodDataResponse.class);
//      Map<String, List<FoodNutrient>> returnMap = new HashMap<>();
//      HttpURLConnection clientConnection;
//      if (listFoods.isEmpty()) {
//        throw new DatasourceException("Food list empty");
//      }
//      for (String food : listFoods) {
//        URL foodURL =
//                new URL(
//                        "https",
//                        "api.nal.usda.gov",
//                        "/fdc/v1/foods/search?dataType=Foundation" +
//                                "&sortBy=dataType.keyword" +
//                                "&pageSize=1" +
//                                "&sortOrder=asc" +
//                                "&api_key=" + apikey.getKey() +
//                                "&query=" + food.replace(" ", "%20"));
//        clientConnection = connect(foodURL);
//        FoodDataResponse response = adapter.fromJson(buffer.readFrom(clientConnection.getInputStream()));
//        if (response == null) {
//          continue;
//        } else if (response.foods == null || response.foods.isEmpty()) { //response.foods.get(0) == null
//          returnMap.put(food, new ArrayList<>());
//        } else {
//          returnMap.put(response.foods.get(0).description, response.foods.get(0).foodNutrients);
//        }
//        clientConnection.disconnect();
//      }
//      return returnMap;
//    } catch (Exception e) {
//      throw new DatasourceException(e.getMessage());
//    }
//  }

//  public record FoodSearchCriteria(
//          @Json(name = "dataType") List<String> dataType,
//          @Json(name = "query") String query,
//          @Json(name = "generalSearchInput") String generalSearchInput,
//          @Json(name = "pageNumber") int pageNumber,
//          @Json(name = "sortBy") String sortBy,
//          @Json(name = "sortOrder") String sortOrder,
//          @Json(name = "numberOfResultsPerPage") int numberOfResultsPerPage,
//          @Json(name = "pageSize") int pageSize,
//          @Json(name = "requireAllWords") boolean requireAllWords,
//          @Json(name = "foodTypes") List<String> foodTypes
//  ) {
//  }
//
//  public record FoodNutrient(
//          @Json(name = "nutrientId") int nutrientId,
//          @Json(name = "nutrientName") String nutrientName,
//          @Json(name = "nutrientNumber") String nutrientNumber,
//          @Json(name = "unitName") String unitName,
//          @Json(name = "derivationCode") String derivationCode,
//          @Json(name = "derivationDescription") String derivationDescription,
//          @Json(name = "derivationId") int derivationId,
//          @Json(name = "value") double value,
//          @Json(name = "foodNutrientSourceId") int foodNutrientSourceId,
//          @Json(name = "foodNutrientSourceCode") String foodNutrientSourceCode,
//          @Json(name = "foodNutrientSourceDescription") String foodNutrientSourceDescription,
//          @Json(name = "rank") int rank,
//          @Json(name = "indentLevel") int indentLevel,
//          @Json(name = "foodNutrientId") int foodNutrientId
//          //@Json(name = "dataPoints") int dataPoints
//  ) {
//  }
//
//  public record Food(
//          @Json(name = "fdcId") int fdcId,
//          @Json(name = "description") String description,
//          @Json(name = "commonNames") String commonNames,
//          @Json(name = "additionalDescriptions") String additionalDescriptions,
//          @Json(name = "dataType") String dataType,
//          @Json(name = "ndbNumber") int ndbNumber,
//          @Json(name = "publishedDate") String publishedDate,
//          @Json(name = "foodCategory") String foodCategory,
//          @Json(name = "allHighlightFields") String allHighlightFields,
//          @Json(name = "score") double score,
//          @Json(name = "microbes") List<String> microbes,
//          @Json(name = "foodNutrients") List<FoodNutrient> foodNutrients
//  ) {
//  }
//
//  public record FoodDataResponse(
//          @Json(name = "totalHits") int totalHits,
//          @Json(name = "currentPage") int currentPage,
//          @Json(name = "totalPages") int totalPages,
//          @Json(name = "pageList") List<Integer> pageList,
//          @Json(name = "foodSearchCriteria") FoodSearchCriteria foodSearchCriteria,
//          @Json(name = "foods") List<Food> foods
//  ) {
//  }
