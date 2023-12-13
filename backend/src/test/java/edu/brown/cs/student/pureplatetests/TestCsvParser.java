package edu.brown.cs.student.pureplatetests;

import edu.brown.cs.student.pureplate.datasources.CsvParser;
import edu.brown.cs.student.pureplate.datasources.DatasourceException;
import java.io.IOException;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.testng.Assert;

public class TestCsvParser {

  @Test
  public void testParse() throws IOException, DatasourceException {
    // fix later -- diff header names
    CsvParser parser = new CsvParser();

    // File doesn't exist
    Assert.assertThrows(DatasourceException.class, () -> parser.parse("notafilename.csv"));

    // The file we want to parse (daily_requirements.csv) can be parsed
    parser.parse("data/nutrition/daily_requirements.csv");
    Map<String, Map<String, Double>> requirementsTable = parser.getTable();
    Assert.assertEquals(requirementsTable.keySet().size(), 2);
    Assert.assertTrue(requirementsTable.containsKey("Male"));
    Assert.assertTrue(requirementsTable.containsKey("Female"));

    Map<String, Double> maleRequirements = requirementsTable.get("Male");
    Map<String, Double> femaleRequirements = requirementsTable.get("Female");
    Assert.assertEquals(maleRequirements.size(), 25);
    Assert.assertEquals(femaleRequirements.size(), 25);

    Assert.assertEquals(maleRequirements.get("Calorie Level Assessed"), 2000);
    Assert.assertEquals(femaleRequirements.get("Calorie Level Assessed"), 1600);

    Assert.assertEquals(maleRequirements.get("Protein (g)"), 56);
    Assert.assertEquals(femaleRequirements.get("Protein (g)"), 46);

    Assert.assertEquals(maleRequirements.get("Fiber (g)"), 28);
    Assert.assertEquals(femaleRequirements.get("Fiber (g)"), 22);

    Assert.assertEquals(maleRequirements.get("Vitamin K (mcg)"), 120);
    Assert.assertEquals(femaleRequirements.get("Vitamin K (mcg)"), 90);

    Assert.assertEquals(maleRequirements.get("Folate (mcg DFE)"), 400);
    Assert.assertEquals(femaleRequirements.get("Folate (mcg DFE)"), 400);

    System.out.println(requirementsTable.get("Male"));
    System.out.println(requirementsTable.get("Female"));
  }

}
