package edu.brown.cs.student.pureplatetests;

import edu.brown.cs.student.pureplate.datasources.DatasourceException;
import edu.brown.cs.student.pureplate.datasources.NutritionDataSource;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.testng.Assert;

/**
 * Testing class for NutritionDataSource.
 */
public class TestNutritionDataSource {

  @Test
  public void testGetFoodDatabase() throws DatasourceException {
    NutritionDataSource dataSource = new NutritionDataSource();
    dataSource.getFoodDatabase();
    Map<String, Map<String, Double>> foodData = dataSource.getFoodData();
    Assert.assertEquals(foodData.size(), 265);

    // Food descriptions as keys
    Assert.assertTrue(foodData.containsKey("Carrots, baby, raw"));
    Assert.assertTrue(foodData.containsKey("Tomato, roma")); // 104 instead of 105
    Assert.assertTrue(foodData.containsKey("Pork, loin, boneless, raw"));
    Assert.assertFalse(foodData.containsKey("not in database"));
    Assert.assertFalse(foodData.containsKey("carrot")); // not a food description

    // Map w/ nutrient name keys and amount values per food description key
    Map<String, Double> carrotNutrients = foodData.get("Carrots, baby, raw");
    Assert.assertEquals(carrotNutrients.size(), 25);
    Assert.assertEquals(carrotNutrients.get("Iron, Fe"), 0.088);
    Assert.assertEquals(carrotNutrients.get("Magnesium, Mg"), 11.1);

    Map<String, Double> tomatoNutrients = foodData.get("Tomato, roma");
    Assert.assertEquals(tomatoNutrients.size(), 37);
    Assert.assertEquals(tomatoNutrients.get("Phosphorus, P"), 19.1);
    Assert.assertEquals(tomatoNutrients.get("Vitamin C, total ascorbic acid"), 17.8);

    Map<String, Double> porkNutrients = foodData.get("Pork, loin, boneless, raw");
    Assert.assertEquals(porkNutrients.size(), 22);
    Assert.assertEquals(porkNutrients.get("Sodium, Na"), 40.2);
    Assert.assertEquals(porkNutrients.get("Fatty acids, total saturated"), 3.28);

    Assert.assertThrows(NullPointerException.class, () -> foodData.get("not in database").get("Sodium, Na"));
    Assert.assertThrows(NullPointerException.class, () -> foodData.get("carrot").get("Iron, Fe"));
  }

//  @Test
//  public void testQuery() throws DatasourceException {
//  }


//  /**
//   * Tests the functionality of the getNutritionData method.
//   * @throws DatasourceException if nutrition data cannot be retrieved.
//   */
//  @Test
//  public void testGetNutritionData() throws DatasourceException {
//    NutritionDataSource dataSource = new NutritionDataSource();
//
//    // Empty list of foods
//    Assert.assertThrows(DatasourceException.class, () -> dataSource.getNutritionData(List.of()));
//
//    // Basic case
//    Map<String, List<FoodNutrient>> nutritionData = dataSource.getNutritionData(List.of("eggs"));
//    Assert.assertTrue(nutritionData.containsKey("Eggs, Grade A, Large, egg white"));
//    List<FoodNutrient> nutrients = nutritionData.get("Eggs, Grade A, Large, egg white");
//    Assert.assertEquals(nutrients.size(), 29);
//    Assert.assertEquals(nutrients.get(0).nutrientId(), 1166);
//
//    // Food with spaces
//    nutritionData = dataSource.getNutritionData(List.of("pork loin"));
//    Assert.assertTrue(nutritionData.containsKey("Pork, loin, boneless, raw"));
//    nutrients = nutritionData.get("Pork, loin, boneless, raw");
//    Assert.assertEquals(nutrients.size(), 22);
//    Assert.assertEquals(nutrients.get(0).nutrientId(), 1089);
//
//    // Multiple entries in list
//    nutritionData = dataSource.getNutritionData(List.of("scrambled eggs", "steak", "apple"));
//    Assert.assertTrue(nutritionData.containsKey("Eggs, Grade A, Large, egg white"));
//    nutrients = nutritionData.get("Eggs, Grade A, Large, egg white");
//    Assert.assertEquals(nutrients.size(), 29);
//    Assert.assertEquals(nutrients.get(0).nutrientId(), 1166);
//
//    Assert.assertTrue(nutritionData.containsKey("Beef, flank, steak, boneless, choice, raw"));
//    nutrients = nutritionData.get("Beef, flank, steak, boneless, choice, raw");
//    Assert.assertEquals(nutrients.size(), 22);
//    Assert.assertEquals(nutrients.get(0).nutrientId(), 1089);
//
//    Assert.assertTrue(nutritionData.containsKey("Apples, fuji, with skin, raw"));
//    nutrients = nutritionData.get("Apples, fuji, with skin, raw");
//    Assert.assertEquals(nutrients.size(), 31);
//    Assert.assertEquals(nutrients.get(0).nutrientId(), 1089);
//
//    // Duplicate items in list
//    nutritionData = dataSource.getNutritionData(List.of("pork loin", "pork loin"));
//    Assert.assertTrue(nutritionData.containsKey("Pork, loin, boneless, raw"));
//    Assert.assertEquals(nutritionData.keySet().size(), 1); // should not result in duplicate map entries
//    nutrients = nutritionData.get("Pork, loin, boneless, raw");
//    Assert.assertEquals(nutrients.size(), 22);
//    Assert.assertEquals(nutrients.get(0).nutrientId(), 1089);
//
//    // Food not in database
//    nutritionData = dataSource.getNutritionData(List.of("salmon"));
//    Assert.assertTrue(nutritionData.containsKey("salmon"));
//    nutrients = nutritionData.get("salmon");
//    Assert.assertTrue(nutrients.isEmpty());
//
//    nutritionData = dataSource.getNutritionData(List.of("asdfghjkl"));
//    Assert.assertTrue(nutritionData.containsKey("asdfghjkl"));
//    nutrients = nutritionData.get("asdfghjkl");
//    Assert.assertTrue(nutrients.isEmpty());
//  }

  /**
   * Tests the functionality of the calculateCaloricRequirement method.
   */
  @Test
  public void testCalculateCaloricRequirement() throws DatasourceException {
    NutritionDataSource dataSource = new NutritionDataSource();

    // Negative weight, height, and age
    Assert.assertThrows(DatasourceException.class, () -> dataSource.calculateCaloricRequirement(-80, 175, 25, "male", "unknown"));
    Assert.assertThrows(DatasourceException.class, () -> dataSource.calculateCaloricRequirement(80, -175, 25, "male", "unknown"));
    Assert.assertThrows(DatasourceException.class, () -> dataSource.calculateCaloricRequirement(80, 175, -25, "male", "unknown"));

    // Vary weight
    double caloricRequirement = dataSource.calculateCaloricRequirement(80, 175, 25, "male", "unknown");
    Assert.assertEquals(caloricRequirement, 10 * 80 + 6.25 * 175 - 5 * 25 + 5);

    caloricRequirement = dataSource.calculateCaloricRequirement(60, 175, 25, "male", "unknown");
    Assert.assertEquals(caloricRequirement, 10 * 60 + 6.25 * 175 - 5 * 25 + 5);

    // Vary height
    caloricRequirement = dataSource.calculateCaloricRequirement(70, 160, 25, "male", "unknown");
    Assert.assertEquals(caloricRequirement, 10 * 70 + 6.25 * 160 - 5 * 25 + 5);

    caloricRequirement = dataSource.calculateCaloricRequirement(70, 180, 25, "male", "unknown");
    Assert.assertEquals(caloricRequirement, 10 * 70 + 6.25 * 180 - 5 * 25 + 5);

    // Vary age
    caloricRequirement = dataSource.calculateCaloricRequirement(70, 170, 15, "male", "unknown");
    Assert.assertEquals(caloricRequirement, 10 * 70 + 6.25 * 170 - 5 * 15 + 5);

    caloricRequirement = dataSource.calculateCaloricRequirement(70, 170, 57, "male", "unknown");
    Assert.assertEquals(caloricRequirement, 10 * 70 + 6.25 * 170 - 5 * 57 + 5);

    // Vary gender
    caloricRequirement = dataSource.calculateCaloricRequirement(70, 175, 25, "male", "unknown");
    Assert.assertEquals(caloricRequirement, 10 * 70 + 6.25 * 175 - 5 * 25 + 5);

    caloricRequirement = dataSource.calculateCaloricRequirement(70, 175, 25, "Male", "unknown");
    Assert.assertEquals(caloricRequirement, 10 * 70 + 6.25 * 175 - 5 * 25 + 5);

    caloricRequirement = dataSource.calculateCaloricRequirement(70, 175, 25, "female", "unknown");
    Assert.assertEquals(caloricRequirement, 10 * 70 + 6.25 * 175 - 5 * 25 - 161);

    caloricRequirement = dataSource.calculateCaloricRequirement(70, 175, 25, "Female", "unknown");
    Assert.assertEquals(caloricRequirement, 10 * 70 + 6.25 * 175 - 5 * 25 - 161);

    // Specific activity levels
    caloricRequirement = dataSource.calculateCaloricRequirement(50, 160, 30, "female", "Sedentary");
    Assert.assertEquals(caloricRequirement, (10 * 50 + 6.25 * 160 - 5 * 30 - 161) * 1.2);

    caloricRequirement = dataSource.calculateCaloricRequirement(50, 160, 30, "female", "Lightly Active");
    Assert.assertEquals(caloricRequirement, (10 * 50 + 6.25 * 160 - 5 * 30 - 161) * 1.375);

    caloricRequirement = dataSource.calculateCaloricRequirement(50, 160, 30, "female", "Moderately Active");
    Assert.assertEquals(caloricRequirement, (10 * 50 + 6.25 * 160 - 5 * 30 - 161) * 1.55);

    caloricRequirement = dataSource.calculateCaloricRequirement(50, 160, 30, "female", "Very Active");
    Assert.assertEquals(caloricRequirement, (10 * 50 + 6.25 * 160 - 5 * 30 - 161) * 1.725);

    caloricRequirement = dataSource.calculateCaloricRequirement(50, 160, 30, "female", "Extra Active");
    Assert.assertEquals(caloricRequirement, (10 * 50 + 6.25 * 160 - 5 * 30 - 161) * 1.9);

    caloricRequirement = dataSource.calculateCaloricRequirement(50, 160, 30, "female", "asdfghjkl");
    Assert.assertEquals(caloricRequirement, 10 * 50 + 6.25 * 160 - 5 * 30 - 161);
  }
}
