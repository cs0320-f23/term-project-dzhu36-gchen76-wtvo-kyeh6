package edu.brown.cs.student.mapserver;

import static spark.Spark.after;

import edu.brown.cs.student.mapserver.datasources.AcsDataSource;
import edu.brown.cs.student.mapserver.datasources.GeoDataSource;
import edu.brown.cs.student.mapserver.datasources.Query;
import edu.brown.cs.student.mapserver.datasources.Cache;

import java.io.IOException;
import spark.Spark;

/**
 * Top-level class that contains the main() method which starts Spark and runs the geodata and
 * broadband handlers through a Server instance.
 */
public class MapsServer {

  /**
   * Constructor for the Server class.
   *
   * @param geoData - a Query to the GeoJson redlining data.
   * @param apiData - a Query that will eventually retrieve broadband percentages from the ACS API.
   */
  public MapsServer(Query<String, String> geoData, Query<String, String> apiData) {
    int port = 3232;

    Spark.port(port);
    after(
        (request, response) -> {
          response.header("Access-Control-Allow-Origin", "*");
          response.header("Access-Control-Allow-Methods", "*");
        });
    Spark.get("geodata", new MapsHandler(geoData));
    Spark.get("broadband", new BroadBandHandler(apiData));
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
  public static void main(String[] args) throws IOException {
    new MapsServer(new Cache(new GeoDataSource("data/geodata/fullDownload.json"), 10, 10),
        new Cache(new AcsDataSource(), 10, 10));
  }
}
