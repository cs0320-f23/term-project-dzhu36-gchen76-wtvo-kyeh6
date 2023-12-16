package edu.brown.cs.student.pureplatetests;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.pureplate.datasources.CsvParser;
import edu.brown.cs.student.pureplate.datasources.DatasourceException;
import edu.brown.cs.student.pureplate.datasources.NutritionDataSource.Food;
import edu.brown.cs.student.pureplate.datasources.NutritionDataSource.FoodNutrient;
import edu.brown.cs.student.pureplate.datasources.Query;
import edu.brown.cs.student.pureplate.key.apikey;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import okio.Buffer;
import org.testng.Assert;

public class MockNutritionDataSource implements Query<String, List<String>> {
  private static Map<String, Map<String, Double>> nutritionalRequirements;
  private Map<String, Double> nutritionNeeds;
  private Map<String, Map<String, Double>> foodData;
  private List<String> visited;
  private List<String> growable = parseGrowable();

  public MockNutritionDataSource(String filename) {
    try {
      this.foodData = new HashMap<>();
      this.nutritionNeeds = new HashMap<>();
      this.getFoodDatabase();
      CsvParser parser = new CsvParser();
      parser.parse(filename);
      nutritionalRequirements = parser.getTable();
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
    //System.out.println("in query");
    String weight = target.get(0);
    String height = target.get(1);
    String age = target.get(2);
    String gender = target.get(3);
    String activity = target.get(4);
    String foodsString = target.get(5);

    this.visited = new ArrayList<>();
    List<String> foodsList = Arrays.asList(foodsString.split("`"));
    this.visited.addAll(foodsList);
    try {
      this.calculateRatios(this.calculateCaloricRequirement(Double.parseDouble(weight), Integer.parseInt(height), Integer.parseInt(age), gender, activity), gender);
    } catch (NumberFormatException e) {
      throw new DatasourceException("Unreasonable weight, height, or age parameter");
    }
    //System.out.println("still in query, passed ratios");
    this.calculateDeficiency(this.visited);
    //System.out.println("still in query, passed calc defs");
    return this.getRecommendations().toString();
  }

  private void getFoodDatabase() {
    this.foodData.put("Carrots, baby, raw", new HashMap<>());
    this.foodData.get("Carrots, baby, raw").put("Water", 89.3);
    this.foodData.get("Carrots, baby, raw").put("Vitamin B-6", 0.115);
    this.foodData.get("Carrots, baby, raw").put("Calcium, Ca", 42.2);

    this.foodData.put("Tomato, roma", new HashMap<>());
    this.foodData.get("Tomato, roma").put("Water", 94.7);
    this.foodData.get("Tomato, roma").put("Carotene, alpha", 0.875);
    this.foodData.get("Tomato, roma").put("Vitamin B-6", 0.0789);

    this.foodData.put("Pork, loin, boneless, raw", new HashMap<>());
    this.foodData.get("Pork, loin, boneless, raw").put("Water", 68.8);
    this.foodData.get("Pork, loin, boneless, raw").put("Fatty acids, total saturated", 3.28);
    this.foodData.get("Pork, loin, boneless, raw").put("Potassium, K", 361.0);
  }

  private Map<String, Double> getScore() {
    //System.out.println("in get score");
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
        }
      }
      scoreMap.put(foodKey,  counter > 0 ? (this.visited.contains(foodKey) ? 99.0 : score/counter) : 0.0 ); // if the food doesn't have the nutrient
    }
    return scoreMap;
  }

  private List<String> getRecommendations() {
    // maybe dont use mocked csv but replace this func instead
    //System.out.println("in get recs");
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

      //System.out.println("Update score map");
      // update Scores
      scoreMap = this.getScore();

      // re-add everything to priority
      priorityFoods = new PriorityQueue<>(
          Comparator.comparingDouble(scoreMap::get));
      priorityFoods.addAll(this.foodData.keySet());
      recommendationList.add(highestPriorityFood);
    }
    //System.out.println("at end of get recs");
    return recommendationList;
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
    //System.out.println("in calc ratios");
    // strings to be able to be converted to double, otherwise number format exception
    // check csv file
    gender = gender.toLowerCase();
    //System.out.println("parsing csv");
    //System.out.println(this.nutritionalRequirements.keySet());
    double caloricRatio = caloricRequirements / this.nutritionalRequirements.get(gender).get("Calorie Level Assessed");
    //System.out.println(caloricRatio);
    for (String key : this.nutritionalRequirements.get(gender).keySet()) {
      this.nutritionNeeds.put(key, this.nutritionalRequirements.get(gender).get(key) / caloricRatio);
    }
  }

  public void calculateDeficiency(List<String> foods){
    //System.out.println("in calc defs");
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
}
