package edu.brown.cs.student.pureplate;

import static spark.Spark.after;

import edu.brown.cs.student.pureplate.datasources.Cache;
import edu.brown.cs.student.pureplate.datasources.DatasourceException;
import edu.brown.cs.student.pureplate.datasources.NutritionDataSource;
import edu.brown.cs.student.pureplate.datasources.Query;
import java.io.IOException;
import java.util.Map;
import spark.Spark;

/**
 * Top-level class that contains the main() method which starts Spark and runs the pureplate
 * handler through a PurePlateServer instance.
 */
public class PurePlateServer {

  /**
   * Constructor for the PurePlateServer class.
   */
  public PurePlateServer(Query<String, String> cache) {
    int port = 3233;

    Spark.port(port);
    after(
        (request, response) -> {
          response.header("Access-Control-Allow-Origin", "*");
          response.header("Access-Control-Allow-Methods", "*");
        });
    Spark.get("pureplate", new PurePlateHandler());
    Spark.init();
    Spark.awaitInitialization();

    // This print statement was kept for easy access to the URL
    System.out.println("Server started at http://localhost:" + port);
  }

  /**
   * The initial method called when execution begins.
   *
   * @param args - an array of program arguments.
   */
  public static void main(String[] args) {
    new PurePlateServer(new Cache(new NutritionDataSource(), 100, 1000));
  }
}
