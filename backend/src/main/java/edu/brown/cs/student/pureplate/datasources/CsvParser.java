package edu.brown.cs.student.pureplate.datasources;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CsvParser {
  private Map<String, Map<String, Double>> table;

  public CsvParser() {
    this.table = new HashMap<>();
  }

  public void parse(String filename) throws DatasourceException {
    try {
      BufferedReader bufferedReader = new BufferedReader(new FileReader(filename));
      String currentFileLine = bufferedReader.readLine();
      List<String> headers = Arrays.asList(currentFileLine.split(","));
      currentFileLine = bufferedReader.readLine();
      int genderIndex = headers.indexOf("gender");
      System.out.println(headers);
      System.out.println(headers.size());

      while (currentFileLine != null) {
        List<String> rowValues = Arrays.asList(currentFileLine.split(","));
        System.out.println(rowValues);
        System.out.println(rowValues.size());
        String gender = rowValues.get(genderIndex);
        this.table.put(gender, new HashMap<>());
        for (int i = 1; i < headers.size(); i++) {
          this.table.get(gender).put(headers.get(i), Double.parseDouble(rowValues.get(i)));
        }
        currentFileLine = bufferedReader.readLine();
      }
      bufferedReader.close();
    } catch (IOException e) {
      throw new DatasourceException("File can't be found");
    }
  }

  // defensive programming
  public Map<String, Map<String, Double>> getTable() {
    return new HashMap<>(this.table);
  }
}
