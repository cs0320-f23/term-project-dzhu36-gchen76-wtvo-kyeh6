package edu.brown.cs.student.pureplate.datasources;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.Moshi;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import okio.BufferedSource;
import okio.Okio;

/**
 * This class searches through a user-inputted JSON file (intended to be the GeoJson redlining data
 * file). It either filters the redlining data based on minimum and maximum latitude and longitude
 * values, filters the data for entries that contain a specific keyword, or returns the entire
 * dataset, unfiltered.
 */
public class GeoDataSource implements Query<String, String> {

  private Map<String, FeatureCollection> prevKeywordData;
  private FeatureCollection featureCollection;

  /**
   * Constructor for GeoDataSource.
   *
   * @param filepath - the JSON file to be searched through.
   * @throws IOException if an error occurs with the file processing.
   */
  public GeoDataSource(String filepath) throws IOException {
    BufferedSource bufferedFile = Okio.buffer(Okio.source(new File(filepath)));
    JsonReader jsonReader = JsonReader.of(bufferedFile);
    Moshi moshi = new Moshi.Builder().build();
    JsonAdapter<FeatureCollection> adapter = moshi.adapter(FeatureCollection.class);
    this.featureCollection = adapter.fromJson(jsonReader);
    this.prevKeywordData = new HashMap<>();
  }

  /**
   * Filters the JSON data based on user-specified parameters.
   *
   * @param target - a String containing any target parameters (e.g., latitudes and longitudes or a
   *               keyword).
   * @return a serialized version of the filtered JSON data.
   */
  @Override
  public String query(String target) {
    String[] coordinates = target.split(",");
    if (coordinates[0].equals("")) {
      return this.serialize(this.featureCollection);
    } else if (coordinates.length == 1) { // assume that only the keyword has been provided
      String keyword = coordinates[0];
      FeatureCollection filteredCollection = filterKeyword(keyword);
      if (!this.prevKeywordData.containsKey(keyword)) {
        this.prevKeywordData.put(keyword, filteredCollection);
      }
      return this.serialize(filteredCollection);
    } else {
      double min_lat = Double.parseDouble(coordinates[0]);
      double max_lat = Double.parseDouble(coordinates[1]);
      double min_long = Double.parseDouble(coordinates[2]);
      double max_long = Double.parseDouble(coordinates[3]);
      FeatureCollection filteredCollection = filterBBox(min_lat, max_lat, min_long, max_long);
      return this.serialize(filteredCollection);
    }
  }

  /**
   * Serializes an inputted FeatureCollection.
   *
   * @param featureCollection - the FeatureCollection to be serialized.
   * @return the serialized featureCollection instance.
   */
  private String serialize(FeatureCollection featureCollection) {
    Moshi moshi = new Moshi.Builder().build();
    JsonAdapter<FeatureCollection> adapter = moshi.adapter(FeatureCollection.class);
    return adapter.toJson(featureCollection);
  }

  /**
   * Filters GeoJson redlining data so that all data entries must lie within a specified minimum and
   * maximum latitude as well as a minimum and maximum longitude.
   *
   * @param min_lat  - a minimum bounding latitude
   * @param max_lat  - a maximum bounding latitude
   * @param min_long - a minimum bounding longitude
   * @param max_long - a maximum bounding longitude
   * @return the filtered GeoJson data.
   */
  private FeatureCollection filterBBox(double min_lat, double max_lat, double min_long,
      double max_long) {
    List<Feature> featureCopy = new ArrayList<>();
    for (int i = 0; i < this.featureCollection.features.size(); i++) {
      Feature currentFeature = this.featureCollection.features.get(i);
      List<List<Double>> coordinates;
      try {
        coordinates = currentFeature.geometry.coordinates.get(0).get(0);
        if (this.inBoundingBox(coordinates, min_lat, max_lat, min_long, max_long)) {
          featureCopy.add(currentFeature);
        }
      } catch (NullPointerException e) {
        continue;
      }
    }
    return new FeatureCollection(this.featureCollection.type, featureCopy);
  }

  /**
   * Filters GeoJson redlining data so that all data entries must have area descriptions that
   * contain a user-inputted keyword.
   *
   * @param keyword - the user-inputted keyword to filter the redlining data on.
   * @return the filtered GeoJson data.
   */
  private FeatureCollection filterKeyword(String keyword) {
    List<Feature> featureCopy = new ArrayList<>();
    for (int i = 0; i < this.featureCollection.features.size(); i++) {
      Feature currentFeature = this.featureCollection.features.get(i);
      Map<String, String> areaDescription;
      try {
        areaDescription = currentFeature.properties.area_description_data;
        if (this.containsKeyword(keyword, areaDescription.values())) {
          featureCopy.add(currentFeature);
        }
      } catch (Exception e) {
        continue;
      }
    }
    return new FeatureCollection(this.featureCollection.type, featureCopy);
  }

  /**
   * A helper method that returns true if a list of coordinates (corresponding to a GeoJson data
   * entry) falls within user-specific minimum and maximum latitude and longitude values and false
   * if otherwise.
   *
   * @param coordinates - a list of coordinates corresponding to a GeoJson data entry.
   * @param min_lat     - the minimum bounding latitude.
   * @param max_lat     - the maximum bounding latitude.
   * @param min_long    - the minimum bounding longitude.
   * @param max_long    - the maximum bounding longitude.
   * @return true if the coordinates fall within the specified bounds and false if not.
   */
  private boolean inBoundingBox(List<List<Double>> coordinates, double min_lat, double max_lat,
      double min_long, double max_long) {
    for (List<Double> coordinate : coordinates) {
      double longitude = coordinate.get(0);
      double latitude = coordinate.get(1);
      if (latitude < min_lat || latitude > max_lat || longitude < min_long
          || longitude > max_long) {
        return false;
      }
    }
    return true;
  }

  /**
   * A helper method that returns true if a collection of area descriptions (each corresponding to a
   * GeoJson data entry) contains a user-specified keyword and false if otherwise.
   *
   * @param keyword           - the keyword to filter the GeoJson data on.
   * @param descriptionValues - a collection of area descriptions (corresponding to one GeoJson data
   *                          entry).
   * @return true if the area description collection contains the keyword.
   */
  private boolean containsKeyword(String keyword, Collection<String> descriptionValues) {
    for (String currDescription : descriptionValues) {
      if (currDescription.contains(keyword)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns a copy of the prevKeywordData Map, which stores previously queried keywords and their
   * corresponding filtered GeoJson redlining data.
   *
   * @return a Map of String keys and FeatureCollection values; a copy of the prevKeywordData
   * instance variable.
   */
  public Map<String, FeatureCollection> getPrevKeywordData() {
    return new HashMap<>(this.prevKeywordData);
  }

  /**
   * A record for the GeoJson data's FeatureCollection type.
   *
   * @param type     - the type of the FeatureCollection represented as a String.
   * @param features - a list of Features for the current FeatureCollection.
   */
  public record FeatureCollection(String type, List<Feature> features) {

  }

  /**
   * A record for the FeatureCollection's features field.
   *
   * @param type       - the type of the features field represented as a String.
   * @param geometry   - features' geometry field represented as a MultiPolygon.
   * @param properties - features' properties field represented as a Property.
   */
  public record Feature(String type, MultiPolygon geometry, Property properties) {

  }

  /**
   * A record for the Feature's geometry field.
   *
   * @param type        - the type of the geometry field represented as a String.
   * @param coordinates - a list of lists of lists of lists of doubles representing the coordinates
   *                    of the current geometry field.
   */
  public record MultiPolygon(String type, List<List<List<List<Double>>>> coordinates) {

  }

  /**
   * A record for the Feature's properties field.
   *
   * @param state                 - the state of the properties field represented as a String.
   * @param city                  - the city of the properties field represented as a String.
   * @param name                  - the name of the properties field represented as a String.
   * @param holc_id               - the HOLC ID of the properties field represented as a String.
   * @param holc_grade            - the HOLC grade of the properties field represented as a String.
   * @param neighborhood_id       - the neighborhood ID of the properties field represented as an
   *                              Integer.
   * @param area_description_data - the area description data of the properties field represented as
   *                              a Map with String keys and String values.
   */
  public record Property(
      String state,
      String city,
      String name,
      String holc_id,
      String holc_grade,
      Integer neighborhood_id,
      Map<String, String> area_description_data
  ) {

  }
}
